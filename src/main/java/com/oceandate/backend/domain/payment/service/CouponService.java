package com.oceandate.backend.domain.payment.service;

import com.oceandate.backend.domain.payment.dto.*;
import com.oceandate.backend.domain.payment.entity.Coupon;
import com.oceandate.backend.domain.payment.entity.MemberCoupon;
import com.oceandate.backend.domain.payment.enums.CouponStatus;
import com.oceandate.backend.domain.payment.repository.CouponRepository;
import com.oceandate.backend.domain.payment.repository.MemberCouponRepository;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CouponService {

    private final CouponRepository couponRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final MemberRepository memberRepository;

    /**
     * 쿠폰 생성
     */
    public CouponResponse createCoupon(CouponRequest request) {
        // 쿠폰 코드 중복 체크
        if (couponRepository.existsByCouponCode(request.getCouponCode())) {
            throw new CustomException(ErrorCode.DUPLICATE_COUPON_CODE);
        }

        // 할인 금액 계산
        int discountAmount = calculateBaseDiscountAmount(
                request.getDiscountType(),
                request.getDiscountValue(),
                request.getMaxDiscountAmount()
        );

        Coupon coupon = Coupon.builder()
                .name(request.getName())
                .couponCode(request.getCouponCode())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount())
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .discountAmount(discountAmount)
                .validDays(request.getValidDays())
                .isActive(true)
                .isWelcomeCoupon(request.getIsWelcomeCoupon() != null ? request.getIsWelcomeCoupon() : false)
                .build();

        couponRepository.save(coupon);
        return CouponResponse.from(coupon);
    }

    /**
     * 쿠폰 수정
     */
    public CouponResponse updateCoupon(Long couponId, CouponRequest request) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

        // 쿠폰 코드 변경 시 중복 체크
        if (!coupon.getCouponCode().equals(request.getCouponCode()) &&
            couponRepository.existsByCouponCode(request.getCouponCode())) {
            throw new CustomException(ErrorCode.DUPLICATE_COUPON_CODE);
        }

        int discountAmount = calculateBaseDiscountAmount(
                request.getDiscountType(),
                request.getDiscountValue(),
                request.getMaxDiscountAmount()
        );

        coupon.setName(request.getName());
        coupon.setCouponCode(request.getCouponCode());
        coupon.setDescription(request.getDescription());
        coupon.setDiscountType(request.getDiscountType());
        coupon.setDiscountValue(request.getDiscountValue());
        coupon.setMinOrderAmount(request.getMinOrderAmount());
        coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        coupon.setDiscountAmount(discountAmount);
        coupon.setValidDays(request.getValidDays());

        if (request.getIsWelcomeCoupon() != null) {
            coupon.setIsWelcomeCoupon(request.getIsWelcomeCoupon());
        }

        return CouponResponse.from(coupon);
    }

    /**
     * 쿠폰 활성화/비활성화
     */
    public void toggleCouponStatus(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));
        coupon.setIsActive(!coupon.getIsActive());
    }

    /**
     * 쿠폰 목록 조회
     */
    public List<CouponResponse> getCoupons(Boolean isActive) {
        List<Coupon> coupons = isActive != null
                ? couponRepository.findByIsActive(isActive)
                : couponRepository.findAll();

        return coupons.stream()
                .map(CouponResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 쿠폰 상세 조회
     */
    public CouponResponse getCouponById(Long couponId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));
        return CouponResponse.from(coupon);
    }

    /**
     * 특정 사용자에게 쿠폰 발급
     */
    public MemberCouponResponse issueCouponToUser(Long couponId, Long memberId) {
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

        if (!coupon.getIsActive()) {
            throw new CustomException(ErrorCode.COUPON_NOT_ACTIVE);
        }

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 이미 발급받았는지 체크
        if (memberCouponRepository.existsByMemberAndCouponId(member, couponId)) {
            throw new CustomException(ErrorCode.COUPON_ALREADY_ISSUED);
        }

        MemberCoupon memberCoupon = MemberCoupon.builder()
                .member(member)
                .coupon(coupon)
                .status(CouponStatus.ISSUED)
                .issuedAt(LocalDateTime.now())
                .expiredAt(LocalDateTime.now().plusDays(coupon.getValidDays()))
                .build();

        memberCouponRepository.save(memberCoupon);
        return MemberCouponResponse.from(memberCoupon);
    }

    /**
     * 여러 사용자에게 쿠폰 발급
     */
    public List<MemberCouponResponse> issueCouponToUsers(CouponIssuanceRequest request) {
        Coupon coupon = couponRepository.findById(request.getCouponId())
                .orElseThrow(() -> new CustomException(ErrorCode.COUPON_NOT_FOUND));

        if (!coupon.getIsActive()) {
            throw new CustomException(ErrorCode.COUPON_NOT_ACTIVE);
        }

        List<Member> members;

        // memberIds가 있으면 특정 사용자들에게 발급
        if (request.getMemberIds() != null && !request.getMemberIds().isEmpty()) {
            members = memberRepository.findAllById(request.getMemberIds());
        }
        // 없으면 전체 사용자에게 발급
        else {
            members = memberRepository.findAll();
        }

        List<MemberCouponResponse> responses = new ArrayList<>();

        for (Member member : members) {
            // 이미 발급받은 사용자는 스킵
            if (memberCouponRepository.existsByMemberAndCouponId(member, coupon.getId())) {
                continue;
            }

            MemberCoupon memberCoupon = MemberCoupon.builder()
                    .member(member)
                    .coupon(coupon)
                    .status(CouponStatus.ISSUED)
                    .issuedAt(LocalDateTime.now())
                    .expiredAt(LocalDateTime.now().plusDays(coupon.getValidDays()))
                    .build();

            memberCouponRepository.save(memberCoupon);
            responses.add(MemberCouponResponse.from(memberCoupon));
        }

        return responses;
    }

    /**
     * 내 사용 가능한 쿠폰 목록 조회
     */
    public List<MemberCouponResponse> getMyUsableCoupons(Long memberId) {
        List<MemberCoupon> coupons = memberCouponRepository.findUsableCouponsByMemberId(
                memberId,
                LocalDateTime.now()
        );

        return coupons.stream()
                .map(MemberCouponResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 내 전체 쿠폰 목록 조회
     */
    public List<MemberCouponResponse> getMyCoupons(Long memberId) {
        List<MemberCoupon> coupons = memberCouponRepository.findByMemberId(memberId);

        return coupons.stream()
                .map(MemberCouponResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 만료된 쿠폰 처리 (배치용)
     */
    public void expireExpiredCoupons() {
        List<MemberCoupon> expiredCoupons = memberCouponRepository.findExpiredCoupons(
                LocalDateTime.now()
        );

        for (MemberCoupon coupon : expiredCoupons) {
            coupon.expire();
        }
    }

    /**
     * 회원가입 시 자동으로 웰컴 쿠폰 발급
     */
    @Transactional
    public void issueWelcomeCoupon(Member member) {
        // 활성화된 회원가입 쿠폰 조회
        couponRepository.findByIsWelcomeCouponAndIsActive(true, true)
                .ifPresent(coupon -> {
                    // 이미 발급받았는지 체크
                    if (!memberCouponRepository.existsByMemberAndCouponId(member, coupon.getId())) {
                        MemberCoupon memberCoupon = MemberCoupon.builder()
                                .member(member)
                                .coupon(coupon)
                                .status(CouponStatus.ISSUED)
                                .issuedAt(LocalDateTime.now())
                                .expiredAt(LocalDateTime.now().plusDays(coupon.getValidDays()))
                                .build();

                        memberCouponRepository.save(memberCoupon);
                    }
                });
    }

    // ============= Private Helper Methods =============

    private int calculateBaseDiscountAmount(
            com.oceandate.backend.domain.payment.enums.DiscountType discountType,
            Integer discountValue,
            Integer maxDiscountAmount
    ) {
        return switch (discountType) {
            case PERCENTAGE -> maxDiscountAmount != null ? maxDiscountAmount : discountValue;
            case FIXED -> discountValue;
        };
    }
}
