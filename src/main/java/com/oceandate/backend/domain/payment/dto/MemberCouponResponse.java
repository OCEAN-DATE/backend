package com.oceandate.backend.domain.payment.dto;

import com.oceandate.backend.domain.payment.entity.MemberCoupon;
import com.oceandate.backend.domain.payment.enums.CouponStatus;
import com.oceandate.backend.domain.payment.enums.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberCouponResponse {

    private Long memberCouponId;
    private Long couponId;
    private String couponName;
    private String couponCode;
    private String description;
    private DiscountType discountType;
    private Integer discountValue;
    private Integer minOrderAmount;
    private Integer maxDiscountAmount;
    private CouponStatus status;
    private LocalDateTime issuedAt;
    private LocalDateTime usedAt;
    private LocalDateTime expiredAt;
    private Boolean isUsable;

    public static MemberCouponResponse from(MemberCoupon memberCoupon) {
        return MemberCouponResponse.builder()
                .memberCouponId(memberCoupon.getId())
                .couponId(memberCoupon.getCoupon().getId())
                .couponName(memberCoupon.getCoupon().getName())
                .couponCode(memberCoupon.getCoupon().getCouponCode())
                .description(memberCoupon.getCoupon().getDescription())
                .discountType(memberCoupon.getCoupon().getDiscountType())
                .discountValue(memberCoupon.getCoupon().getDiscountValue())
                .minOrderAmount(memberCoupon.getCoupon().getMinOrderAmount())
                .maxDiscountAmount(memberCoupon.getCoupon().getMaxDiscountAmount())
                .status(memberCoupon.getStatus())
                .issuedAt(memberCoupon.getIssuedAt())
                .usedAt(memberCoupon.getUsedAt())
                .expiredAt(memberCoupon.getExpiredAt())
                .isUsable(memberCoupon.isUsable())
                .build();
    }
}
