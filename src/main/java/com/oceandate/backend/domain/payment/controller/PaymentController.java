package com.oceandate.backend.domain.payment.controller;

import com.oceandate.backend.domain.payment.dto.PaymentCancelRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmRequest;
import com.oceandate.backend.domain.payment.dto.PaymentConfirmResponse;
import com.oceandate.backend.domain.payment.dto.TossPaymentResponse;
import com.oceandate.backend.domain.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payment")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(summary = "결제 승인")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @RequestBody PaymentConfirmRequest request
            ){
        PaymentConfirmResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }

//    @Operation(summary = "결제 내역 조회")
//    @GetMapping("/{orderId}")
//    public ResponseEntity<String> getPayment(
//            @AuthenticationPrincipal Long userId,
//            @PathVariable String orderId
//    ){
//        String response = paymentService.getPaymentByOrderId(userId, orderId);
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_JSON)
//                .body(response);
//    }
}
