package com.oceandate.backend.domain.payment.repository;

import com.oceandate.backend.domain.payment.entity.MemberCoupon;
import com.oceandate.backend.domain.payment.enums.CouponStatus;
import com.oceandate.backend.domain.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    // 사용자의 사용 가능한 쿠폰 목록 조회
    @Query("SELECT mc FROM MemberCoupon mc " +
           "WHERE mc.member.id = :memberId " +
           "AND mc.status = 'ISSUED' " +
           "AND mc.expiredAt > :now")
    List<MemberCoupon> findUsableCouponsByMemberId(
            @Param("memberId") Long memberId,
            @Param("now") LocalDateTime now
    );

    // 사용자의 전체 쿠폰 목록 조회
    List<MemberCoupon> findByMemberId(Long memberId);

    // 특정 쿠폰 코드로 사용자 쿠폰 조회
    @Query("SELECT mc FROM MemberCoupon mc " +
           "JOIN mc.coupon c " +
           "WHERE mc.member.id = :memberId " +
           "AND c.couponCode = :couponCode " +
           "AND mc.status = 'ISSUED' " +
           "AND mc.expiredAt > :now")
    Optional<MemberCoupon> findUsableCouponByCode(
            @Param("memberId") Long memberId,
            @Param("couponCode") String couponCode,
            @Param("now") LocalDateTime now
    );

    // 만료된 쿠폰 조회 (배치 처리용)
    @Query("SELECT mc FROM MemberCoupon mc " +
           "WHERE mc.status = 'ISSUED' " +
           "AND mc.expiredAt <= :now")
    List<MemberCoupon> findExpiredCoupons(@Param("now") LocalDateTime now);

    // 사용자가 특정 쿠폰을 이미 받았는지 확인
    boolean existsByMemberAndCouponId(Member member, Long couponId);
}
