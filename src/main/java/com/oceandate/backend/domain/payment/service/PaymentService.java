package com.oceandate.backend.domain.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.payment.client.TossPaymentClient;
import com.oceandate.backend.domain.payment.dto.PaymentCancelRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmResponse;
import com.oceandate.backend.domain.payment.util.TossErrorMapper;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
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

    private final OneToOneRepository oneToOneRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request){
        OneToOne application = oneToOneRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

        if(application.getStatus() == ApplicationStatus.PAYMENT_COMPLETED){
            return PaymentConfirmResponse.from(application);
        }

        if(application.getStatus() != ApplicationStatus.PAYMENT_PENDING){
            throw new CustomException(ErrorCode.INVALID_PAYMENT_STATUS);
        }

        if(!application.getAmount().equals(request.getAmount())){
            throw new CustomException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }

        application.setStatus(ApplicationStatus.PAYMENT_PROCESSING);
        oneToOneRepository.saveAndFlush(application);

        try {
            HttpResponse<String> response = tossPaymentClient.requestConfirm(request);

            if (response.statusCode() == 200) {
                PaymentConfirmResponse confirmResponse = objectMapper.readValue(
                        response.body(),
                        PaymentConfirmResponse.class
                );

                application.setPaymentKey(request.getPaymentKey());
                application.setStatus(ApplicationStatus.PAYMENT_COMPLETED);
                oneToOneRepository.save(application);

                log.info("결제 승인 완료 - orderId: {}, paymentKey: {}",
                        request.getOrderId(), request.getPaymentKey());

                return confirmResponse;

            } else {
                ObjectMapper objectMapper = new ObjectMapper();
                String tossErrorCode = objectMapper.readTree(response.body()).get("code").asText();
                throw new CustomException(TossErrorMapper.fromTossErrorCode(tossErrorCode));
            }
        }
        catch (Exception e) {
            log.error("결제 승인 실패 - orderId: {}, error: {}",
                    request.getOrderId(), e.getMessage());
            throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
        }
    }

    public String getPaymentByOrderId(Long userId, String orderId) {
        OneToOne application = oneToOneRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if(!application.getMember().getId().equals(userId)){
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        try {
            HttpResponse<String> response = tossPaymentClient.getPaymentByOrderId(orderId);

            if(response.statusCode() == 200){
                return response.body();
            }
            else{
                ObjectMapper objectMapper = new ObjectMapper();
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

    public String cancelPayment(Long userId, PaymentCancelRequest request) {
//        OneToOne application = oneToOneRepository.findByPaymentKey(request.getPaymentKey())
//                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
//
//        if(application.getMember().getId().equals(userId)){
//            throw new CustomException(ErrorCode.ACCESS_DENIED);
//        }

        try {
            HttpResponse<String> response = tossPaymentClient.cancelPayment(request);

            if(response.statusCode() == 200){
                return response.body();
            }
            else {
                ObjectMapper objectMapper = new ObjectMapper();
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
