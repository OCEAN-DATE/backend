package com.oceandate.backend.domain.user.service;

import com.oceandate.backend.domain.payment.service.CouponService;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import com.oceandate.backend.global.oauth2.userinfo.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final CouponService couponService;

    @Transactional
    public Member registerMember(SocialUserInfo userInfo) {
        // 이메일로 기존 회원 확인
        if (memberRepository.existsByEmail(userInfo.getEmail())) {
            return memberRepository.findByEmail(userInfo.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("회원 조회 실패"));
        }

        // 신규 회원 등록
        Member newMember = Member.from(userInfo);
        Member savedMember = memberRepository.save(newMember);

        // 회원가입 쿠폰 자동 발급
        try {
            couponService.issueWelcomeCoupon(savedMember);
        } catch (Exception e) {
            // 쿠폰 발급 실패해도 회원가입은 성공으로 처리
            // 로그만 남기고 예외는 무시
        }

        return savedMember;
    }

    public Member findById(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return member;
    }
}
