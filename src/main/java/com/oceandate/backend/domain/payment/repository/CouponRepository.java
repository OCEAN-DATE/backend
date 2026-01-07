package com.oceandate.backend.domain.payment.repository;

import com.oceandate.backend.domain.payment.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
}
