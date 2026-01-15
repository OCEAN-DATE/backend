package com.oceandate.backend.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oceandate.backend.domain.payment.enums.BankCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@JsonInclude
public class PaymentCancelRequest {
    String paymentKey;
    String cancelReason;
    Integer cancelAmount;
    RefundReceiveAccount account;
    Integer taxFreeAmount;
    String currency;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class RefundReceiveAccount{
        private String bank;
        private String accountNumber;
        private String holderName;
    }
}
