package com.oceandate.backend.domain.payment.controller;

import com.oceandate.backend.domain.payment.client.TossPaymentClient;
import com.oceandate.backend.domain.payment.dto.ConfirmPaymentRequest;
import com.oceandate.backend.domain.payment.dto.SaveAmountRequest;
import com.oceandate.backend.domain.payment.dto.TossPaymentResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.net.http.HttpResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final TossPaymentClient tossPaymentClient;
    private final ObjectMapper objectMapper;

    @PostMapping("/saveAmount")
    public ResponseEntity<?> tempsave(HttpSession session, @RequestBody SaveAmountRequest saveAmountRequest){
        session.setAttribute(saveAmountRequest.getOrderId(), saveAmountRequest.getAmount());
        return ResponseEntity.ok("Payment temp saved successfully");
    }

    @PostMapping("/verifyAmount")
    public ResponseEntity<?> verifyAmount(HttpSession session, @RequestBody SaveAmountRequest saveAmountRequest){
        BigDecimal amount = (BigDecimal) session.getAttribute(saveAmountRequest.getOrderId());

        if(amount == null || amount != saveAmountRequest.getAmount()){
            return ResponseEntity.badRequest().body("결제 금액 정보가 유효하지 않습니다.");
        }

        session.removeAttribute(saveAmountRequest.getOrderId());

        return ResponseEntity.ok("Payment is valid");
    }

    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(
            HttpSession session,
            @RequestBody ConfirmPaymentRequest confirmPaymentRequest) throws Exception {

        try {
            BigDecimal savedAmount = (BigDecimal) session.getAttribute(confirmPaymentRequest.getOrderId());

            if (savedAmount == null || !savedAmount.equals(confirmPaymentRequest.getAmount())) {
                return ResponseEntity.badRequest().body("결제 금액 정보가 유효하지 않습니다.");
            }

            HttpResponse<String> response = tossPaymentClient.requestConfirm(confirmPaymentRequest);

            if (response.statusCode() == 200) {
                TossPaymentResponse tossResponse =
                        objectMapper.readValue(response.body(), TossPaymentResponse.class);

                session.removeAttribute(confirmPaymentRequest.getOrderId());

                return ResponseEntity.ok(tossResponse);
            }
            else{
                TossPaymentResponse.Failure failureResponse =
                        objectMapper.readValue(response.body(), TossPaymentResponse.Failure.class);

                return ResponseEntity.status(response.statusCode()).body(failureResponse);
            }
        }
        catch(Exception e){
            return ResponseEntity.internalServerError()
                    .body("결제 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
