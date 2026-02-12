package com.oceandate.backend.domain.payment.dto;

import com.oceandate.backend.domain.payment.entity.Coupon;
import com.oceandate.backend.domain.payment.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponResponse {

    private Long id;
    private String name;
    private String couponCode;
    private String description;
    private DiscountType discountType;
    private Integer discountValue;
    private Integer minOrderAmount;
    private Integer maxDiscountAmount;
    private Integer discountAmount;
    private Integer validDays;
    private Boolean isActive;
    private Boolean isWelcomeCoupon;

    public static CouponResponse from(Coupon coupon) {
        return CouponResponse.builder()
                .id(coupon.getId())
                .name(coupon.getName())
                .couponCode(coupon.getCouponCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .discountAmount(coupon.getDiscountAmount())
                .validDays(coupon.getValidDays())
                .isActive(coupon.getIsActive())
                .isWelcomeCoupon(coupon.getIsWelcomeCoupon())
                .build();
    }
}
