package com.oceandate.backend.domain.payment.repository;

import com.oceandate.backend.domain.payment.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCouponCode(String couponCode);

    boolean existsByCouponCode(String couponCode);

    List<Coupon> findByIsActive(Boolean isActive);

    Optional<Coupon> findByIsWelcomeCouponAndIsActive(Boolean isWelcomeCoupon, Boolean isActive);
}
