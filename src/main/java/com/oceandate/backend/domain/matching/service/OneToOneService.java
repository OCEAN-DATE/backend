package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.admin.dto.response.CancelRequestList;
import com.oceandate.backend.domain.matching.dto.CancelResponse;
import com.oceandate.backend.domain.matching.dto.OneToOneResponse;
import com.oceandate.backend.domain.matching.dto.UserInfo;
import com.oceandate.backend.domain.matching.dto.OneToOneRequest;
import com.oceandate.backend.domain.matching.entity.CancelRequest;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.CancelRequestStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.CancelRequestRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneEventRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneMatchingRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.payment.dto.PaymentCancelRequest;
import com.oceandate.backend.domain.payment.entity.Payment;
import com.oceandate.backend.domain.payment.enums.PaymentStatus;
import com.oceandate.backend.domain.payment.repository.PaymentRepository;
import com.oceandate.backend.domain.payment.service.PaymentService;
import com.oceandate.backend.domain.payment.util.RefundPolicy;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import com.oceandate.backend.global.jwt.AccountContext;
import com.oceandate.backend.global.sms.SmsService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OneToOneService {

    private final OneToOneRepository oneToOneRepository;
    private final OneToOneEventRepository oneToOneEventRepository;
    private final OneToOneMatchingRepository matchingRepository;
    private final MemberRepository memberRepository;
    private final PaymentRepository paymentRepository;
    private final CancelRequestRepository cancelRequestRepository;
    private final PaymentService paymentService;
    private final SmsService smsService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    @Transactional
    public void createApplication(Long userId, OneToOneRequest request){

        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        OneToOneEvent event = oneToOneEventRepository.findById(request.getEventId())
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        if(event.getStatus() != EventStatus.OPEN){
            throw new CustomException(ErrorCode.EVENT_CLOSED);
        }

        if(oneToOneRepository.existsByMemberIdAndEventId(userId, event.getId())){
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }

        String orderId = "onetoone_" + UUID.randomUUID().toString();

        OneToOne application = OneToOne.builder()
                .member(user)
                .event(event)
                .preferredDates(request.getPreferredDates())
                .job(request.getJob())
                .status(ApplicationStatus.APPLICATION_SUBMITTED)
                .introduction(request.getIntroduction())
                .idealType(request.getIdealType())
                .hobby(request.getHobby())
                .orderId(orderId)
                .build();

        try {
            oneToOneRepository.save(application);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }
    }

    public List<OneToOneResponse> getApplications(ApplicationStatus status) {
        List<OneToOne> applications;

        if(status == null){
            applications = oneToOneRepository.findAll();
        }
        else{
            applications = oneToOneRepository.findByStatus(status);
        }

        return applications.stream()
                .map(OneToOneResponse::from)
                .collect(Collectors.toList());
    }

    public OneToOneResponse getMyApplicationDetail(Long userId, Long applicationId){
        OneToOne application = oneToOneRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        return OneToOneResponse.fromDetail(application, UserInfo.from(application));
    }

    public OneToOneResponse getApplicationDetail(Long applicationId){
        OneToOne application = oneToOneRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        UserInfo applicantInfo = UserInfo.from(application);

        if (!application.getStatus().isBeforeMatched()) {
            UserInfo partnerInfo = getMatchedPartner(applicationId);
            return OneToOneResponse.fromMatched(application, applicantInfo, partnerInfo);
        }

        return OneToOneResponse.fromDetail(application, applicantInfo);
    }

    private UserInfo getMatchedPartner(Long applicationId) {
        return matchingRepository.findByApplicationId(applicationId)
                .map(matching -> {
                    OneToOne partner = matching.getMaleApplication().getId().equals(applicationId)
                            ? matching.getFemaleApplication()
                            : matching.getMaleApplication();
                    return UserInfo.from(partner);
                })
                .orElse(null);
    }

    @Transactional
    public void updateStatus(Long id, ApplicationStatus status) {
        OneToOne application = oneToOneRepository.findById(id)
                .orElseThrow((() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다.")));

        if(status == ApplicationStatus.APPROVED){
            application.setApprovedAt(LocalDateTime.now());

            // 승인 시 결제 링크가 포함된 SMS 전송
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

    public List<OneToOneResponse> getMyApplications(Long userId) {
        List<OneToOne> applications = oneToOneRepository.findByMemberId(userId);

        return applications.stream()
                .map(OneToOneResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public CancelResponse requestCancel(
            Long applicationId,
            String cancelReason,
            AccountContext accountContext
    ) {
        OneToOne application = oneToOneRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if (!application.getMember().getId().equals(accountContext.getMemberId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (!application.getStatus().isCancellable()) {
            throw new CustomException(ErrorCode.INVALID_CANCEL_STATUS);
        }

        cancelRequestRepository.findByApplicationIdAndStatus(applicationId, CancelRequestStatus.PENDING)
                .ifPresent(cr -> { throw new CustomException(ErrorCode.CANCEL_REQUEST_ALREADY_EXISTS); });

        if (application.getStatus().isBeforeMatched()) {
            application.setStatus(ApplicationStatus.CANCELLED);
            application.setCancelledAt(LocalDateTime.now());
            application.setCancelReason(cancelReason);
            return CancelResponse.immediate(applicationId);
        }

        Member requester = memberRepository.findById(accountContext.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        CancelRequest cancelRequest = CancelRequest.create(application, requester, cancelReason);
        cancelRequestRepository.save(cancelRequest);

        application.setStatus(ApplicationStatus.CANCEL_REQUESTED);

        return CancelResponse.pending(applicationId, cancelRequest.getId());
    }

    private RefundPolicy.RefundAmount calculateRefund(OneToOne application) {
        if (!application.getStatus().isRefundRequired()) {
            return new RefundPolicy.RefundAmount(0, 0, "결제 전 취소");
        }

        Payment payment = paymentRepository.findByOrderId(application.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        LocalDateTime eventDate = application.getConfirmedDate();
        LocalDateTime paymentDate = payment.getPaidAt();

        return RefundPolicy.calculate(
                eventDate,
                paymentDate,
                LocalDateTime.now(),
                payment.getFinalAmount()
        );
    }

    private void processPaymentCancel(
            AccountContext accountContext,
            OneToOne target,
            RefundPolicy.RefundAmount refund
    ) {
        Payment payment = paymentRepository.findByOrderId(target.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        PaymentCancelRequest cancelRequest = PaymentCancelRequest.builder()
                .orderId(payment.getOrderId())
                .cancelReason(refund.getReason())
                .cancelAmount(refund.getAmount())
                .build();

        paymentService.cancelPayment(accountContext, cancelRequest);
    }

    public OneToOneResponse getPreviousApplication(AccountContext accountContext) {
        return oneToOneRepository
                .findFirstByMemberIdOrderByCreatedAtDesc(accountContext.getMemberId())
                .map(OneToOneResponse::from)
                .orElse(OneToOneResponse.empty());
    }

    public List<CancelRequestList> getPendingRequests() {
        return cancelRequestRepository.findByStatusOrderByCreatedAtAsc(CancelRequestStatus.PENDING)
                .stream()
                .map(CancelRequestList::of)
                .collect(Collectors.toList());
    }

    @Transactional
    public void approveCancelRequest(
            Long cancelRequestId,
            AccountContext adminContext
    ) {
        CancelRequest cancelRequest = cancelRequestRepository.findById(cancelRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.CANCEL_REQUEST_NOT_FOUND));

        if (cancelRequest.getStatus() != CancelRequestStatus.PENDING) {
            throw new CustomException(ErrorCode.CANCEL_REQUEST_ALREADY_PROCESSED);
        }

        OneToOne application = cancelRequest.getApplication();

        Payment payment = paymentRepository.findByOrderId(application.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.PAYMENT_NOT_FOUND));

        RefundPolicy.RefundAmount refund = calculateRefund(application);

        if (refund.getAmount() > 0) {
            processPaymentCancel(adminContext, application, refund);
        }

        application.cancel(cancelRequest.getCancelReason(), refund.getAmount());
        payment.setRefundedAt(LocalDateTime.now());
        payment.setRefundAmount(refund.getAmount());
        payment.setStatus(PaymentStatus.CANCELLED);

        cancelRequest.approve((long)refund.getAmount());
    }

    @Transactional
    public void rejectCancelRequest(
            Long cancelRequestId,
            String adminComment
    ) {
        CancelRequest cancelRequest = cancelRequestRepository.findById(cancelRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.CANCEL_REQUEST_NOT_FOUND));

        if (cancelRequest.getStatus() != CancelRequestStatus.PENDING) {
            throw new CustomException(ErrorCode.CANCEL_REQUEST_ALREADY_PROCESSED);
        }

        OneToOne application = cancelRequest.getApplication();
        application.setStatus(cancelRequest.getPreviousStatus());

        cancelRequest.reject(adminComment);
    }
}
