package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.OneToOneResponse;
import com.oceandate.backend.domain.matching.dto.UserInfo;
import com.oceandate.backend.domain.matching.dto.OneToOneRequest;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.entity.OneToOneMatching;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneEventRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneMatchingRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.payment.dto.PaymentCancelRequest;
import com.oceandate.backend.domain.payment.dto.RefundResponse;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    private final OneToOneMatchingRepository oneToOneMatchingRepository;
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
                .amount(event.getAmount())
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

        if (application.getStatus() == ApplicationStatus.MATCHED) {
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
    public RefundResponse cancelApplication(
            Long applicationId,
            String userCancelReason,
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

        // 환불 계산
        RefundPolicy.RefundAmount refund = calculateRefund(application);

        // 결제 취소 처리
        if (refund.getAmount() > 0) {
            processPaymentCancel(accountContext, application, refund);
        }

        // 본인 취소
        String cancelReason = userCancelReason != null ? userCancelReason : "사용자 요청";
        application.cancel(cancelReason, refund.getAmount());

        // MATCHED 상태면 상대방도 취소 + 전액 환불
        if (application.getStatus() == ApplicationStatus.MATCHED) {
            cancelCounterpart(application, accountContext);
        }

        return RefundResponse.of(applicationId, refund.getAmount(), refund.getRate(), refund.getReason());
    }

    private RefundPolicy.RefundAmount calculateRefund(OneToOne application) {
        if (!application.getStatus().isRefundRequired()) {
            return new RefundPolicy.RefundAmount(0, 0, "결제 전 취소");
        }

        LocalDateTime eventDate = application.getConfirmedDate();
        LocalDateTime paymentDate = application.getPaidAt();

        return RefundPolicy.calculate(
                eventDate,
                paymentDate,
                LocalDateTime.now(),
                application.getAmount()
        );
    }

    private void cancelCounterpart(OneToOne application, AccountContext accountContext) {
        OneToOneMatching matching = oneToOneMatchingRepository
                .findByMaleApplicationOrFemaleApplication(application, application)
                .orElseThrow(() -> new CustomException(ErrorCode.MATCHING_NOT_FOUND));

        OneToOne counterpart = matching.getMaleApplication().equals(application)
                ? matching.getFemaleApplication()
                : matching.getMaleApplication();

        if (!counterpart.getStatus().isCancellable()) {
            return;
        }

        RefundPolicy.RefundAmount counterpartRefund = RefundPolicy.fullRefundByCounterpart(
                counterpart.getAmount()
        );

        if (counterpartRefund.getAmount() > 0) {
            processPaymentCancel(accountContext, counterpart, counterpartRefund);
        }

        counterpart.cancel("상대방 취소로 인한 자동 취소", counterpartRefund.getAmount());
    }

    private void processPaymentCancel(
            AccountContext accountContext,
            OneToOne target,
            RefundPolicy.RefundAmount refund
    ) {
        PaymentCancelRequest cancelRequest = PaymentCancelRequest.builder()
                .paymentKey(target.getPaymentKey())
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
}
