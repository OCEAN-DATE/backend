package com.oceandate.backend.domain.payment.controller;

import com.oceandate.backend.domain.payment.dto.*;
import com.oceandate.backend.domain.payment.service.PaymentService;
import com.oceandate.backend.global.jwt.AccountContext;
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

    @Operation(summary = "결제 준비", description = "쿠폰 적용 후 결제할 금액을 db에 저장합니다. 결제 승인 호출 전 이 api를 먼저 호출해주세요")
    @PostMapping("/prepare")
    public ResponseEntity<PaymentPrepareResponse> preparePayment(
            @AuthenticationPrincipal AccountContext accountContext,
            @RequestBody PaymentPrepareRequest request) {

        PaymentPrepareResponse response = paymentService.preparePayment(accountContext, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 승인")
    @PostMapping("/confirm")
    public ResponseEntity<PaymentConfirmResponse> confirmPayment(
            @RequestBody PaymentConfirmRequest request
            ){
        PaymentConfirmResponse response = paymentService.confirmPayment(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "결제 내역 조회")
    @GetMapping("/{orderId}")
    public ResponseEntity<String> getPayment(
            @AuthenticationPrincipal AccountContext accountContext,
            @PathVariable String orderId
    ){
        String response = paymentService.getPaymentByOrderId(accountContext, orderId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @Operation(summary = "결제 취소")
    @PostMapping("/cancel")
    public ResponseEntity<String> cancelPayment(
            @AuthenticationPrincipal AccountContext accountContext,
            @RequestBody PaymentCancelRequest request
    ){
        String response = paymentService.cancelPayment(accountContext, request);
        return ResponseEntity.ok()
                . contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }
}
