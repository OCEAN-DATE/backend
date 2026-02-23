package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.*;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.RotationEventRepository;
import com.oceandate.backend.domain.matching.repository.RotationRepository;
import com.oceandate.backend.domain.payment.dto.PaymentCancelRequest;
import com.oceandate.backend.domain.payment.dto.RefundResponse;
import com.oceandate.backend.domain.payment.entity.Payment;
import com.oceandate.backend.domain.payment.enums.PaymentStatus;
import com.oceandate.backend.domain.payment.repository.PaymentRepository;
import com.oceandate.backend.domain.payment.service.PaymentService;
import com.oceandate.backend.domain.payment.util.RefundPolicy;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.entity.Sex;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import com.oceandate.backend.global.jwt.AccountContext;
import com.oceandate.backend.global.sms.SmsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RotationService {

    private final RotationRepository rotationRepository;
    private final RotationEventRepository rotationEventRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SmsService smsService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public RotationResponse createApplication(
            Long userId,
            RotationRequest request) {

        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        RotationEvent event = rotationEventRepository.findByIdWithLock(request.getEventId())
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.OPEN) {
            throw new CustomException(ErrorCode.EVENT_CLOSED);
        }

        boolean alreadyApplied = rotationRepository.existsByMemberAndEvent(user, event);
        if (alreadyApplied) {
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }

        if (Sex.MAN.equals(user.getSex()) && !event.canApproveMale()) {
            throw new CustomException(ErrorCode.MALE_CAPACITY_FULL);
        }
        if (Sex.WOMAN.equals(user.getSex()) && !event.canApproveFemale()) {
            throw new CustomException(ErrorCode.FEMALE_CAPACITY_FULL);
        }

        String orderId = "rotation_" + UUID.randomUUID().toString();

        Rotation application = Rotation.builder()
                .member(user)
                .event(event)
                .job(request.getJob())
                .introduction(request.getIntroduction())
                .orderId(orderId)
                .status(ApplicationStatus.PAYMENT_PENDING)
                .build();

        rotationRepository.save(application);

        return RotationResponse.builder()
                .id(application.getId())
                .orderId(orderId)
                .amount(event.getAmount())
                .build();
    }

    @Transactional
public void updateStatus(Long id, ApplicationStatus status) {
    Rotation application = rotationRepository.findById(id)
            .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
    
    if (status == ApplicationStatus.APPROVED) {
        // 인원 카운트 증가 및 승인 시간/확정 날짜 세팅
        application.getEvent().incrementApprovedCount(application.getMember().getSex());
        application.setApprovedAt(LocalDateTime.now());
        application.setConfirmedDate(application.getEvent().getEventDateTime()); // main 로직

        // 승인 시 결제 링크가 포함된 SMS 전송 (feat/login 로직)
        String paymentUrl = String.format("%s/payment/%s", frontendUrl, application.getOrderId());
        smsService.sendPaymentLinkSms(
                application.getMember().getPhoneNumber(),
                application.getMember().getName(),
                application.getOrderId(),
                paymentUrl
        );
    }
    application.setStatus(status);
}

    public List<RotationResponse> getApplications(Long eventId, ApplicationStatus status) {
        List<Rotation> applications;
        if(status == null){
            applications = rotationRepository.findByEventId(eventId);
        }
        else {
            applications = rotationRepository.findByEventIdAndStatus(eventId, status);
        }
        return applications.stream()
                .map(app -> RotationResponse.fromDetail(app, UserInfo.from(app)))
                .toList();
    }

    public List<RotationResponse> getMyApplications(AccountContext accountContext) {
        List<Rotation> applications = rotationRepository.findByMemberId(accountContext.getMemberId());

        return applications.stream()
                .map(RotationResponse::from)
                .toList();
    }

    public List<RotationEventResponse> getEvents(EventStatus status) {
        List<RotationEvent> events;
        if(status == null){
            events = rotationEventRepository.findByStatusNot(EventStatus.DELETED);
        }
        else {
            events = rotationEventRepository.findByStatus(status);
        }

        return events.stream()
                .map(RotationEventResponse::from)
                .toList();
    }

    public List<RotationResponse> getApprovedMembers(Long eventId) {
        List<Rotation> applications = rotationRepository.findByEventIdAndApproved(eventId);

        return applications.stream()
                .map(RotationResponse::from)
                .toList();
    }

    public RotationResponse getApplicationDetail(Long applicationId) {
        Rotation application = rotationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        UserInfo applicantInfo = UserInfo.builder()
                .userId(application.getMember().getId())
                .name(application.getMember().getName())
                .email(application.getMember().getEmail())
                .job(application.getJob())
                .introduction(application.getIntroduction())
                .build();

        return RotationResponse.fromDetail(application, applicantInfo);
    }

    @Transactional
    public RefundResponse cancelApplications(Long eventId, Long applicationId, String cancelReason, AccountContext accountContext) {
        Rotation application = rotationRepository.findByEventIdAndApplicationId(eventId, applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getMember().getId().equals(accountContext.getMemberId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (!application.getStatus().isCancellable()) {
            throw new CustomException(ErrorCode.INVALID_CANCEL_STATUS);
        }

        int refundAmount = 0;
        int refundRate = 0;
        String refundReason = "결제 전 취소";

        if (application.getStatus().isRefundRequired()) {
           Payment payment = paymentRepository.findByOrderId(application.getOrderId())
                   .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

            LocalDateTime paymentDate = payment.getPaidAt();

            RefundPolicy.RefundAmount refund = RefundPolicy.calculate(
                    application.getConfirmedDate(),
                    paymentDate,
                    LocalDateTime.now(),
                    payment.getFinalAmount()
            );

            refundAmount = refund.getAmount();
            refundRate = refund.getRate();
            refundReason = refund.getReason();

            if (refundAmount > 0) {
                PaymentCancelRequest cancelRequest = PaymentCancelRequest.builder()
                        .orderId(payment.getOrderId())
                        .cancelReason(refundReason)
                        .cancelAmount(refundAmount)
                        .build();

                paymentService.cancelPayment(accountContext, cancelRequest);
            }

            payment.setRefundAmount(refundAmount);
            payment.setRefundedAt(LocalDateTime.now());
            payment.setStatus(PaymentStatus.CANCELLED);
        }

        if (application.getStatus() == ApplicationStatus.APPROVED ||
                application.getStatus() == ApplicationStatus.PAYMENT_PENDING ||
                application.getStatus() == ApplicationStatus.PAYMENT_COMPLETED) {
            RotationEvent event = application.getEvent();
            event.decrementApprovedCount(application.getMember().getSex());
        }

        application.cancel(cancelReason != null ? cancelReason : "사용자 요청", refundAmount);

        return RefundResponse.of(applicationId, refundAmount, refundRate, refundReason);
    }

/**
     * 관리자용 - 로테이션 소개팅 취소 (feat/login)
     */
    @Transactional
    public void cancelApplication(Long applicationId, String reason) {
        Rotation application = rotationRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        // 승인된 신청인 경우 인원 수 감소
        if (application.getStatus() == ApplicationStatus.APPROVED || 
            application.getStatus() == ApplicationStatus.PAYMENT_COMPLETED) {
            RotationEvent event = application.getEvent();
            // 주의: 이전에 발생한 타입 에러 해결을 위해 필요시 .name() 추가
            event.decrementApprovedCount(application.getMember().getSex()); 
        }

        // 취소 처리
        application.cancel(reason, 0);
    }

    /**
     * 이전 신청 정보 조회 (main)
     */
    public RotationResponse getPreviousApplication(AccountContext accountContext) {
        return rotationRepository
                .findFirstByMemberIdOrderByCreatedAtDesc(accountContext.getMemberId())
                .map(RotationResponse::from)
                .orElse(RotationResponse.empty());
    }
}
