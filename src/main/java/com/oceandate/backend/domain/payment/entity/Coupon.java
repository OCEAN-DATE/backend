package com.oceandate.backend.domain.payment.entity;

import com.oceandate.backend.domain.payment.enums.DiscountType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "coupon_code", nullable = false, unique = true)
    private String couponCode;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    @Column(nullable = false)
    private Integer discountValue;

    @Column
    private Integer minOrderAmount;

    @Column(name = "max_discount_amount")
    private Integer maxDiscountAmount;  // 최대 할인 금액 (정률 할인 시 사용)

    @Column(name = "discount_amount", nullable = false)
    private Integer discountAmount;

    @Column(name = "valid_days", nullable = false)
    private Integer validDays;  // 발급 후 유효 기간 (일)

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;  // 쿠폰 활성화 여부

    @Column(name = "is_welcome_coupon", nullable = false)
    private Boolean isWelcomeCoupon = false;  // 회원가입 쿠폰 여부

    /**
     * 실제 할인 금액 계산
     */
    public int calculateDiscountAmount(int orderAmount) {
        if (minOrderAmount != null && orderAmount < minOrderAmount) {
            return 0;
        }

        int discount = switch (discountType) {
            case PERCENTAGE -> (int) (orderAmount * discountValue / 100.0);
            case FIXED -> discountValue;
        };

        // 최대 할인 금액 적용
        if (maxDiscountAmount != null && discount > maxDiscountAmount) {
            discount = maxDiscountAmount;
        }

        return discount;
    }
}
