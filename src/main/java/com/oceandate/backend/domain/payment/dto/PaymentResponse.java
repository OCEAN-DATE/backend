package com.oceandate.backend.domain.payment.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private String paymentKey;
    private String orderId;
    private Long amount;
    private String status;
    private LocalDateTime approvedAt;
}
