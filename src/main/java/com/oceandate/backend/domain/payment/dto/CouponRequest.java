package com.oceandate.backend.domain.payment.dto;

import com.oceandate.backend.domain.payment.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponRequest {

    private String name;
    private String couponCode;
    private String description;
    private DiscountType discountType;
    private Integer discountValue;
    private Integer minOrderAmount;
    private Integer maxDiscountAmount;
    private Integer validDays;  // 발급 후 유효 기간 (일)
    private Boolean isWelcomeCoupon;  // 회원가입 쿠폰 여부
}
