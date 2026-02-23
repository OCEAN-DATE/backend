package com.oceandate.backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentPrepareResponse {
    private int finalAmount;
}