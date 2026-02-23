package com.oceandate.backend.domain.payment.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oceandate.backend.domain.payment.dto.PaymentCancelRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmRequest;
import com.oceandate.backend.domain.payment.entity.Payment;
import com.oceandate.backend.domain.payment.repository.PaymentRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Value("${toss.api.secret-key}")
    private String secretKey;

    private String getAuthorization() {
        String credentials = secretKey + ":";
        return "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());
    }

    // 결제 승인
    public HttpResponse<String> requestConfirm(PaymentConfirmRequest request)
            throws IOException, InterruptedException {

        ObjectNode requestObj = objectMapper.createObjectNode()
                .put("paymentKey", request.getPaymentKey())
                .put("orderId", request.getOrderId())
                .put("amount", request.getAmount());

        String requestBody = objectMapper.writeValueAsString(requestObj);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    // 결제 조회
    public HttpResponse<String> getPaymentByOrderId(String orderId)
            throws IOException, InterruptedException {

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/orders/" + orderId))
                .header("Authorization", getAuthorization())
                .GET()
                .build();

        return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    // 결제 취소
    public HttpResponse<String> cancelPayment(PaymentCancelRequest request)
            throws IOException, InterruptedException {

        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PAYMENT));

        ObjectNode requestObj = objectMapper.createObjectNode()
                .put("cancelReason", request.getCancelReason());

        if (request.getCancelAmount() != null) {
            requestObj.put("cancelAmount", request.getCancelAmount());
        }

        String requestBody = objectMapper.writeValueAsString(requestObj);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/"
                        + payment.getPaymentKey() + "/cancel"))
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    // 롤백용 - paymentKey로 직접 취소
    public HttpResponse<String> cancelPaymentByPaymentKey(String paymentKey, String cancelReason)
            throws IOException, InterruptedException {

        ObjectNode requestObj = objectMapper.createObjectNode()
                .put("cancelReason", cancelReason);

        String requestBody = objectMapper.writeValueAsString(requestObj);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/"
                        + paymentKey + "/cancel"))
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}