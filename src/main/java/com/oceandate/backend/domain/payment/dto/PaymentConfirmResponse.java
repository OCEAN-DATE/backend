package com.oceandate.backend.domain.payment.dto;

import com.oceandate.backend.domain.matching.entity.Matching;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.payment.entity.Payment;
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
    private LocalDateTime paidAt;
    private PaymentMethod method;

    public static PaymentConfirmResponse from(Payment payment, Matching application) {
        return PaymentConfirmResponse.builder()
                .orderId(application.getOrderId())
                .paymentKey(payment.getPaymentKey())
                .amount(payment.getFinalAmount())
                .status(application.getStatus())
                .paidAt(payment.getPaidAt())
                .build();
    }
}
