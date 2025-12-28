package com.oceandate.backend.domain.payment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.payment.client.TossPaymentClient;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmResponse;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.http.HttpResponse;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PaymentService {

    private final OneToOneRepository oneToOneRepository;
    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    @Value("${toss.payments.api-url}")
    private String apiUrl;

    public PaymentConfirmResponse confirmPayment(PaymentConfirmRequest request){
        OneToOne application = oneToOneRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.ORDER_NOT_FOUND));

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

                application.setPaymentKey(request.getPaymentKey());
                application.setStatus(ApplicationStatus.PAYMENT_COMPLETED);
                oneToOneRepository.save(application);

                log.info("결제 승인 완료 - orderId: {}, paymentKey: {}",
                        request.getOrderId(), request.getPaymentKey());

                return confirmResponse;

            } else {
                log.error("토스 페이먼츠 API 에러 - orderId: {}, status: {}, body: {}",
                        request.getOrderId(), response.statusCode(), response.body());
                throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
            }

        } catch (Exception e) {
            log.error("결제 승인 실패 - orderId: {}, error: {}",
                    request.getOrderId(), e.getMessage());
            throw new CustomException(ErrorCode.PAYMENT_CONFIRMATION_FAILED);
        }
    }
}
