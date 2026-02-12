package com.oceandate.backend.domain.payment.entity;

import com.oceandate.backend.domain.payment.enums.CouponStatus;
import com.oceandate.backend.domain.user.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "member_coupon")
public class MemberCoupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponStatus status;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(name = "expired_at", nullable = false)
    private LocalDateTime expiredAt;

    @Column(name = "order_id")
    private String orderId;  // 사용된 주문 ID

    @PrePersist
    protected void onCreate() {
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
        if (status == null) {
            status = CouponStatus.ISSUED;
        }
    }

    public void use(String orderId) {
        this.status = CouponStatus.USED;
        this.usedAt = LocalDateTime.now();
        this.orderId = orderId;
    }

    public void expire() {
        this.status = CouponStatus.EXPIRED;
    }

    public boolean isUsable() {
        return status == CouponStatus.ISSUED &&
               LocalDateTime.now().isBefore(expiredAt);
    }
}
