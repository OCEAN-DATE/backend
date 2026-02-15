package com.oceandate.backend.domain.payment.dto;

import com.oceandate.backend.domain.matching.enums.MatchingType;
import lombok.Data;

@Data
public class PaymentPrepareRequest {
    private String orderId;
    private MatchingType matchingType;
    private Long memberCouponId;
}
