package com.oceandate.backend.global.sms;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.oceandate.backend.global.config.SmsProps;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final SmsProps smsProps;
    private final ObjectMapper objectMapper;

    /**
     * SMS 전송
     * @param toPhoneNumber 수신자 전화번호
     * @param message 메시지 내용
     */
    public void sendSms(String toPhoneNumber, String message) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis());
            String signature = makeSignature(timestamp);

            String url = String.format("https://sens.apigw.ntruss.com/sms/v2/services/%s/messages",
                    smsProps.getServiceId());

            Map<String, Object> body = Map.of(
                    "type", "SMS",
                    "contentType", "COMM",
                    "countryCode", "82",
                    "from", smsProps.getFromPhoneNumber(),
                    "content", message,
                    "messages", List.of(
                            Map.of("to", toPhoneNumber)
                    )
            );

            String requestBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json; charset=utf-8")
                    .header("x-ncp-apigw-timestamp", timestamp)
                    .header("x-ncp-iam-access-key", smsProps.getAccessKey())
                    .header("x-ncp-apigw-signature-v2", signature)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 202) {
                log.info("SMS 전송 성공: {}", toPhoneNumber);
            } else {
                log.error("SMS 전송 실패: {} - {}", response.statusCode(), response.body());
            }
        } catch (Exception e) {
            log.error("SMS 전송 중 오류 발생", e);
        }
    }

    /**
     * 네이버 클라우드 SENS API 서명 생성
     */
    private String makeSignature(String timestamp) throws Exception {
        String space = " ";
        String newLine = "\n";
        String method = "POST";
        String url = "/sms/v2/services/" + smsProps.getServiceId() + "/messages";

        String message = method + space + url + newLine + timestamp + newLine + smsProps.getAccessKey();

        SecretKeySpec signingKey = new SecretKeySpec(smsProps.getSecretKey().getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(signingKey);

        byte[] rawHmac = mac.doFinal(message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(rawHmac);
    }

    /**
     * 결제 링크 SMS 전송
     */
    public void sendPaymentLinkSms(String toPhoneNumber, String userName, String orderId, String paymentUrl) {
        String message = String.format(
                "[오션데이트] %s님, 소개팅 신청이 승인되었습니다.\n\n" +
                "아래 링크를 통해 결제를 진행해주세요.\n" +
                "%s\n\n" +
                "주문번호: %s",
                userName,
                paymentUrl,
                orderId
        );
        sendSms(toPhoneNumber, message);
    }
}
