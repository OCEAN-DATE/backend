package com.oceandate.backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RefundResponse {
    private Long applicationId;
    private int refundAmount;
    private int refundRate;
    private String refundReason;
    private String message;

    public static RefundResponse of(Long applicationId, int refundAmount, int refundRate, String reason) {
        return RefundResponse.builder()
                .applicationId(applicationId)
                .refundAmount(refundAmount)
                .refundRate(refundRate)
                .refundReason(reason)
                .message(String.format("%d원이 환불 처리됩니다.", refundAmount))
                .build();
    }
}
