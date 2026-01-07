package com.oceandate.backend.domain.payment.dto;

import com.oceandate.backend.domain.matching.enums.MatchingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private BigDecimal amount;
    private MatchingType matchingType;
}
