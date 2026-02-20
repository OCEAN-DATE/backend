package com.oceandate.backend.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceandate.backend.domain.matching.entity.Matching;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.Travel;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.matching.repository.RotationRepository;
import com.oceandate.backend.domain.matching.repository.TravelRepository;
import com.oceandate.backend.domain.payment.client.PortOnePaymentClient;
import com.oceandate.backend.domain.payment.dto.*;
import com.oceandate.backend.domain.payment.entity.MemberCoupon;
import com.oceandate.backend.domain.payment.entity.Payment;
import com.oceandate.backend.domain.payment.enums.PaymentStatus;
import com.oceandate.backend.domain.payment.repository.MemberCouponRepository;
import com.oceandate.backend.domain.payment.repository.PaymentRepository;
import com.oceandate.backend.domain.payment.util.PortOneErrorMapper;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.entity.Role;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import com.oceandate.backend.global.jwt.AccountContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

    private final MemberRepository memberRepository;
    private final OneToOneRepository oneToOneRepository;
    private final RotationRepository rotationRepository;
    private final TravelRepository travelRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final PaymentRepository paymentRepository;
    private final PortOnePaymentClient portOnePaymentClient;
    private final ObjectMapper objectMapper;

    @Transactional
    public PaymentPrepareResponse preparePayment(AccountContext accountContext, PaymentPrepareRequest request) {
        Member member = memberRepository.findById(accountContext.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Matching application =
        switch (request.getMatchingType()) {
            case ONE_TO_ONE -> oneToOneRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
            case ROTATION -> rotationRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
            case TRAVEL -> travelRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
        };

        if (!application.getMember().getId().equals(member.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        int originalAmount = switch (request.getMatchingType()) {
            case ONE_TO_ONE -> ((OneToOne) application).getEvent().getAmount();
            case ROTATION -> ((Rotation) application).getEvent().getAmount();
            case TRAVEL -> ((Travel) application).getEvent().getAmount();
        };

        int finalAmount = originalAmount;
        MemberCoupon memberCoupon = null;

        if (request.getMemberCouponId() != null) {
            memberCoupon = memberCouponRepository.findById(request.getMemberCouponId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

            if (!memberCoupon.getMember().getId().equals(member.getId())) {
                throw new CustomException(ErrorCode.ACCESS_DENIED);
            }

            if (!memberCoupon.isUsable()) {
                throw new CustomException(ErrorCode.COUPON_NOT_USABLE);
            }

            int discount = memberCoupon.getCoupon().calculateDiscountAmount(finalAmount);
            finalAmount -= discount;
        }

        Optional<Payment> paymentOpt = paymentRepository.findByOrderId(application.getOrderId());

        if(paymentOpt.isPresent()){
            Payment payment = paymentOpt.get();

            if(payment.getStatus() == PaymentStatus.COMPLETED){
                throw new CustomException(ErrorCode.ALREADY_PROCESSED_PAYMENT);
            }

            payment.setOriginalAmount(originalAmount);
            payment.setFinalAmount(finalAmount);
            payment.setMemberCoupon(memberCoupon);
            payment.setStatus(PaymentStatus.PENDING);
            application.setStatus(ApplicationStatus.PAYMENT_PENDING);

            return new PaymentPrepareResponse(finalAmount);
        }
        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .matchingType(request.getMatchingType())
                .originalAmount(originalAmount)
                .finalAmount(finalAmount)
                .memberCoupon(memberCoupon)
                .status(PaymentStatus.PENDING)
                .build();

        paymentRepository.save(payment);
        application.setStatus(ApplicationStatus.PAYMENT_PENDING);

        return new PaymentPrepareResponse(finalAmount);
    }

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request) {

        Matching application = switch (request.getMatchingType()) {
            case ONE_TO_ONE -> oneToOneRepository.findByOrderIdWithLock(request.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
            case ROTATION -> rotationRepository.findByOrderIdWithLock(request.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
            case TRAVEL -> travelRepository.findByOrderId(request.getOrderId())
                    .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
            default -> throw new CustomException(ErrorCode.INVALID_MATCHING_TYPE);
        };

        return processPayment(application, request);
    }

    private <T extends Matching> PaymentConfirmResponse processPayment(T application, PaymentConfirmRequest request) {

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        // 사전 검증 - 포트원 호출 전이므로 롤백 불필요
        if (application.getStatus() == ApplicationStatus.PAYMENT_COMPLETED) {
            if (!payment.getPaymentKey().equals(request.getPaymentId())) {
                throw new CustomException(ErrorCode.PAYMENT_KEY_MISMATCH);
            }
            return PaymentConfirmResponse.from(payment, application);
        }

        if (application.getStatus() != ApplicationStatus.PAYMENT_PENDING) {
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        if (!payment.getFinalAmount().equals(request.getAmount())) {
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        // 포트원 호출 - 여기서부터 예외 나면 롤백 시도
        try {
            HttpResponse<String> response = portOnePaymentClient.requestConfirm(request);

            if (response.statusCode() == 200) {
                PaymentConfirmResponse confirmResponse = objectMapper.readValue(
                        response.body(), PaymentConfirmResponse.class);

                payment.setPaymentKey(request.getPaymentId());
                payment.setStatus(PaymentStatus.COMPLETED);
                payment.setPaidAt(LocalDateTime.now());

                if (payment.getMemberCoupon() != null) {
                    payment.getMemberCoupon().use(request.getOrderId());
                }

                application.setStatus(ApplicationStatus.PAYMENT_COMPLETED);

                try {
                    paymentRepository.flush();
                } catch (Exception dbException) {
                    log.error("DB 저장 실패, 결제 취소 시도 - orderId: {}", request.getOrderId(), dbException);
                    rollbackSafely(request.getOrderId());
                    throw new CustomException(ErrorCode.PAYMENT_DB_SAVE_FAILED);
                }

                return confirmResponse;

            } else {
                // 포트원이 에러 반환 → 승인 안 된 것이므로 롤백 불필요
                String errorType = objectMapper.readTree(response.body()).get("type").asText();
                throw new CustomException(PortOneErrorMapper.fromPortOneErrorType(errorType));
            }

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            // 네트워크 오류 등 → 승인 됐을 수도 있으니 롤백 시도
            log.error("결제 승인 중 예외 발생, 롤백 시도 - orderId: {}", request.getOrderId(), e.getMessage());
            rollbackSafely(request.getOrderId());
            throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
        }
    }

    private void rollbackSafely(String orderId) {
        try {
            rollbackPortOnePayment(orderId, "결제 오류로 인한 자동 취소");
            log.info("결제 자동 취소 완료 - orderId: {}", orderId);
        } catch (Exception cancelException) {
            log.error("결제 취소 실패! 수동 처리 필요 - orderId: {}", orderId, cancelException);
        }
    }

    private void rollbackPortOnePayment(String orderId, String cancelReason) throws Exception {
        HttpResponse<String> cancelResponse = portOnePaymentClient.cancelPaymentByOrderId(orderId, cancelReason);

        if (cancelResponse.statusCode() != 200) {
            JsonNode errorBody = objectMapper.readTree(cancelResponse.body());
            String errorType = errorBody.get("type").asText();
            String errorMessage = errorBody.get("message").asText();
            throw new Exception(String.format("취소 API 실패 [%s]: %s", errorType, errorMessage));
        }
    }


    public String getPaymentByOrderId(AccountContext accountContext, String orderId) {
        Member member = memberRepository.findById(accountContext.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        Matching application = switch (payment.getMatchingType()) {
            case ONE_TO_ONE -> oneToOneRepository.findByOrderIdWithLock(orderId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
            case ROTATION -> rotationRepository.findByOrderIdWithLock(orderId)
                    .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
            case TRAVEL -> travelRepository.findByOrderId(orderId)
                    .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
            default -> throw new CustomException(ErrorCode.INVALID_MATCHING_TYPE);
        };

        if(!member.getRole().equals(Role.ADMIN) && !application.getMember().getId().equals(member.getId())){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        try {
            HttpResponse<String> response = portOnePaymentClient.getPaymentByOrderId(orderId);

            if(response.statusCode() == 200){
                return response.body();
            }
            else{
                String errorType = objectMapper.readTree(response.body()).get("type").asText();
                throw new CustomException(PortOneErrorMapper.fromPortOneErrorType(errorType));
            }
        }
        catch(CustomException e){
            throw e;
        }
        catch(Exception e){
            log.error("결제 조회 실패 - orderId: {}", orderId, e);
            throw new CustomException(ErrorCode.PROVIDER_ERROR);
        }
    }

    public String cancelPayment(AccountContext accountContext, PaymentCancelRequest request) {
        Member member = memberRepository.findById(accountContext.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        Matching application =
                switch (payment.getMatchingType()) {
                    case ONE_TO_ONE -> oneToOneRepository.findByOrderId(payment.getOrderId())
                            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
                    case ROTATION -> rotationRepository.findByOrderId(payment.getOrderId())
                            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
                    case TRAVEL -> travelRepository.findByOrderId(payment.getOrderId())
                            .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));
                };

        if(!member.getRole().equals(Role.ADMIN) && !application.getMember().getId().equals(member.getId())){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (application.getStatus() != ApplicationStatus.PAYMENT_COMPLETED
                && application.getStatus() != ApplicationStatus.MATCHED
                && application.getStatus() != ApplicationStatus.CANCEL_REQUESTED) {
            throw new CustomException(ErrorCode.INVALID_CANCEL_STATUS);
        }

        try {
            HttpResponse<String> response = portOnePaymentClient.cancelPayment(request);

            if (response.statusCode() == 200) {
                payment.setStatus(PaymentStatus.CANCELLED);
                payment.setRefundedAt(LocalDateTime.now());
                payment.setRefundAmount(request.getCancelAmount() != null
                        ? request.getCancelAmount()
                        : payment.getFinalAmount());
                application.setStatus(ApplicationStatus.CANCELLED);
                return response.body();
            } else {
                String errorType = objectMapper.readTree(response.body()).get("type").asText();
                throw new CustomException(PortOneErrorMapper.fromPortOneErrorType(errorType));
            }
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.PROVIDER_ERROR);
        }
    }
}
