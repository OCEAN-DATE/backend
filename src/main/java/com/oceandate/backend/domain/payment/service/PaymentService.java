package com.oceandate.backend.domain.payment.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.payment.client.TossPaymentClient;
import com.oceandate.backend.domain.payment.dto.PaymentCancelRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmResponse;
import com.oceandate.backend.domain.payment.util.TossErrorMapper;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import com.oceandate.backend.global.jwt.AccountContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

    private final MemberRepository memberRepository;
    private final OneToOneRepository oneToOneRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request){
        OneToOne application = oneToOneRepository.findByOrderIdWithLock(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if(application.getStatus() == ApplicationStatus.PAYMENT_COMPLETED){
            if(!application.getPaymentKey().equals(request.getPaymentKey())){
                throw new CustomException(ErrorCode.PAYMENT_KEY_MISMATCH);
            }
            return PaymentConfirmResponse.from(application);
        }

        if(application.getStatus() != ApplicationStatus.PAYMENT_PENDING){
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        if(!application.getAmount().equals(request.getAmount())){
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        try {
            HttpResponse<String> response = tossPaymentClient.requestConfirm(request);

            if (response.statusCode() == 200) {
                PaymentConfirmResponse confirmResponse = objectMapper.readValue(
                        response.body(),
                        PaymentConfirmResponse.class
                );

                try {
                    application.setPaymentKey(request.getPaymentKey());
                    application.setStatus(ApplicationStatus.PAYMENT_COMPLETED);
                    oneToOneRepository.save(application);
                    oneToOneRepository.flush();  // 즉시 DB 반영하여 에러 조기 발견

                    log.info("결제 승인 완료 - orderId: {}, paymentKey: {}",
                            request.getOrderId(), request.getPaymentKey());

                    return confirmResponse;

                } catch (Exception dbException) {
                    log.error("DB 저장 실패, 토스 결제 취소 시도 - orderId: {}, paymentKey: {}",
                            request.getOrderId(), request.getPaymentKey(), dbException);

                    try {
                        rollbackTossPayment(request.getPaymentKey(), "DB 저장 실패로 인한 자동 취소");
                        log.info("토스 결제 자동 취소 완료 - paymentKey: {}", request.getPaymentKey());

                    } catch (Exception cancelException) {
                        log.error(" 토스 결제 취소 실패! 수동 처리 필요 - paymentKey: {}, orderId: {}",
                                request.getPaymentKey(), request.getOrderId(), cancelException);
                    }

                    throw new CustomException(ErrorCode.PAYMENT_DB_SAVE_FAILED);
                }

            } else {
                String tossErrorCode = objectMapper.readTree(response.body()).get("code").asText();
                throw new CustomException(TossErrorMapper.fromTossErrorCode(tossErrorCode));
            }
        }
        catch (CustomException e){
            throw e;
        }
        catch (Exception e) {
            log.error("결제 승인 실패 - orderId: {}, error: {}",
                    request.getOrderId(), e.getMessage());
            throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
        }
    }

    private void rollbackTossPayment(String paymentKey, String cancelReason) throws Exception {
        PaymentCancelRequest cancelRequest = PaymentCancelRequest.builder()
                .paymentKey(paymentKey)
                .cancelReason(cancelReason)
                .build();

        HttpResponse<String> cancelResponse = tossPaymentClient.cancelPayment(cancelRequest);

        if (cancelResponse.statusCode() != 200) {
            JsonNode errorBody = objectMapper.readTree(cancelResponse.body());
            String errorCode = errorBody.get("code").asText();
            String errorMessage = errorBody.get("message").asText();

            throw new Exception(String.format("토스 취소 API 실패 [%s]: %s", errorCode, errorMessage));
        }
    }


    public String getPaymentByOrderId(AccountContext accountContext, String orderId) {
        Member member = memberRepository.findById(accountContext.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        OneToOne application = oneToOneRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if(!application.getMember().getId().equals(member.getId())){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        try {
            HttpResponse<String> response = tossPaymentClient.getPaymentByOrderId(orderId);

            if(response.statusCode() == 200){
                return response.body();
            }
            else{
                String tossErrorCode = objectMapper.readTree(response.body()).get("code").asText();
                throw new CustomException(TossErrorMapper.fromTossErrorCode(tossErrorCode));
            }
        }
        catch(CustomException e){
            throw e;
        }
        catch(Exception e){
            log.error("토스 결제 조회 실패 - orderId: {}", orderId, e);
            throw new CustomException(ErrorCode.PROVIDER_ERROR);
        }
    }

    public String cancelPayment(AccountContext accountContext, PaymentCancelRequest request) {
        Member member = memberRepository.findById(accountContext.getMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        OneToOne application = oneToOneRepository.findByPaymentKey(request.getPaymentKey())
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if(!application.getMember().getId().equals(member.getId())){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if(application.getStatus() != ApplicationStatus.PAYMENT_COMPLETED){
            throw new CustomException(ErrorCode.INVALID_CANCEL_STATUS);
        }

        try {
            HttpResponse<String> response = tossPaymentClient.cancelPayment(request);

            if(response.statusCode() == 200){
                application.setStatus(ApplicationStatus.CANCELLED);
                return response.body();
            }
            else {
                String tossErrorCode = objectMapper.readTree(response.body()).get("code").asText();
                throw new CustomException(TossErrorMapper.fromTossErrorCode(tossErrorCode));
            }
        }
        catch(CustomException e){
            throw e;
        }
        catch(Exception e){
            throw new CustomException(ErrorCode.PROVIDER_ERROR);
        }
    }
}
