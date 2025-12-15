package com.oceandate.backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaveAmountRequest {
    String orderId;
    BigDecimal amount;
}
