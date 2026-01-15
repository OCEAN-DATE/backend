package com.oceandate.backend.domain.payment.dto;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.payment.enums.PaymentMethod;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentConfirmResponse {
    private String paymentKey;
    private String orderId;
    private ApplicationStatus status;
    private Integer amount;
    private LocalDateTime approvedAt;
    private PaymentMethod method;

    public static PaymentConfirmResponse from(OneToOne application) {
        return PaymentConfirmResponse.builder()
                .orderId(application.getOrderId())
                .paymentKey(application.getPaymentKey())
                .amount(application.getAmount())
                .status(application.getStatus())
                .approvedAt(application.getUpdatedAt())
                .build();
    }
}
