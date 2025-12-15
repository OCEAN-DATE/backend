package com.oceandate.backend.domain.payment.controller;

import com.oceandate.backend.domain.payment.dto.SaveAmountRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

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

    
}
