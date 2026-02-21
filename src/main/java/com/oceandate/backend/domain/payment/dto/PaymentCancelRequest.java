package com.oceandate.backend.domain.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude
public class PaymentCancelRequest {
    String orderId;
    String cancelReason;
    Integer cancelAmount;
}
