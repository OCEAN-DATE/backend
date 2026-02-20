package com.oceandate.backend.domain.payment.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oceandate.backend.domain.payment.dto.PaymentCancelRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmRequest;
import com.oceandate.backend.domain.payment.entity.Payment;
import com.oceandate.backend.domain.payment.repository.PaymentRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PortOnePaymentClient {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper objectMapper;

    @Value("${portone.api.secret}")
    private String apiSecret;

    private String getAuthorization() {
        return "PortOne " + apiSecret;
    }

    // 수동 승인
    public HttpResponse<String> requestConfirm(PaymentConfirmRequest request)
            throws IOException, InterruptedException {

        JsonNode requestObj = objectMapper.createObjectNode()
                .put("paymentToken", request.getPaymentToken());

        String requestBody = objectMapper.writeValueAsString(requestObj);

        log.info("포트원 CONFIRM 요청 - paymentId: {}, body: {}", request.getPaymentId(), requestBody);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.portone.io/payments/"
                        + request.getPaymentId() + "/confirm"))
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());

        log.info("포트원 CONFIRM 응답 - status: {}, body: {}", response.statusCode(), response.body());

        return response;
    }

    // 결제 조회
    public HttpResponse<String> getPaymentByOrderId(String orderId)
            throws IOException, InterruptedException {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_PAYMENT));

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.portone.io/payments/"
                        + payment.getPaymentKey()))
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
                .put("reason", request.getCancelReason());

        if (request.getCancelAmount() != null) {
            requestObj.put("amount", request.getCancelAmount());
        }

        String requestBody = objectMapper.writeValueAsString(requestObj);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.portone.io/payments/"
                        + payment.getPaymentKey() + "/cancel"))
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }

    //롤백용 결제 취소
    public HttpResponse<String> cancelPaymentByOrderId(String orderId, String cancelReason)
            throws IOException, InterruptedException {

        ObjectNode requestObj = objectMapper.createObjectNode()
                .put("reason", cancelReason);

        String requestBody = objectMapper.writeValueAsString(requestObj);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://api.portone.io/payments/" + orderId + "/cancel"))
                .header("Authorization", getAuthorization())
                .header("Content-Type", "application/json")
                .header("Idempotency-Key", UUID.randomUUID().toString())
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HttpClient.newHttpClient().send(httpRequest, HttpResponse.BodyHandlers.ofString());
    }
}