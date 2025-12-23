package com.oceandate.backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaveAmountRequest {
    String orderId;
    BigDecimal amount;

    private ReservationType reservationType;
    private Long reservationId;
}
