package com.oceandate.backend.domain.payment.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.oceandate.backend.domain.payment.dto.PaymentCancelRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class TossPaymentClient {

    @Value("${toss.payments.secret-key}")
    private String secretKey;

    private final ObjectMapper objectMapper;

    private String getAuthorizations() {
        return "Basic " + Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes());
    }

    public HttpResponse<String> requestConfirm(PaymentConfirmRequest confirmPaymentRequest)
            throws IOException, InterruptedException {
        String tossOrderId = confirmPaymentRequest.getOrderId();
        Integer amount = confirmPaymentRequest.getAmount();
        String tossPaymentKey = confirmPaymentRequest.getPaymentKey();

        JsonNode requestObj = objectMapper.createObjectNode()
                .put("orderId", tossOrderId)
                .put("amount", amount)
                .put("paymentKey", tossPaymentKey);

        String requestBody = objectMapper.writeValueAsString(requestObj);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/confirm"))
                .header("Authorization", getAuthorizations())
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> getPaymentByOrderId(String orderId)
            throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/orders/" + orderId))
                .header("Authorization", getAuthorizations())
                .method("GET", HttpRequest.BodyPublishers.noBody())
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }

    public HttpResponse<String> cancelPayment(String paymentKey, PaymentCancelRequest paymentCancelRequest)
            throws IOException, InterruptedException {
        String requestBody = objectMapper.writeValueAsString(paymentCancelRequest);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.tosspayments.com/v1/payments/" + paymentKey + "/cancel"))
                .header("Authorization", getAuthorizations())
                .header("Content-Type", "application/json")
                .method("POST", HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        return HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
    }


}
