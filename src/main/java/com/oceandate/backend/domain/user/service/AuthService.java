package com.oceandate.backend.domain.user.service;

import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.domain.user.repository.RefreshTokenRepository;
import com.oceandate.backend.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final CookieUtil cookieUtil;

    /**
     * 로그아웃
     * - RefreshToken DB에서 삭제
     * - 쿠키 삭제 (access_token, refresh_token)
     */
    @Transactional
    public void logout(Long memberId, HttpServletRequest request, HttpServletResponse response) {
        log.info("로그아웃 시작 - memberId: {}", memberId);

        // 1. RefreshToken DB에서 삭제
        if (refreshTokenRepository.existsByMemberId(memberId)) {
            refreshTokenRepository.deleteByMemberId(memberId);
            log.info("RefreshToken 삭제 완료 - memberId: {}", memberId);
        }

        // 2. 쿠키 삭제
        cookieUtil.deleteCookie(request, response, "access_token");
        cookieUtil.deleteCookie(request, response, "refresh_token");
        log.info("쿠키 삭제 완료 - memberId: {}", memberId);

        log.info("로그아웃 완료 - memberId: {}", memberId);
    }

    @Transactional
    public void withdraw(Long memberId, HttpServletRequest request, HttpServletResponse response) {
        log.info("회원 탈퇴 시작 - memberId: {}", memberId);

        // 1. 회원 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 2. 이미 탈퇴한 회원인지 확인
        if (member.isDeleted()) {
            throw new IllegalStateException("이미 탈퇴한 회원입니다.");
        }

        // 3. 회원 탈퇴 처리 (Soft Delete)
        member.withdraw();
        log.info("회원 탈퇴 처리 완료 - memberId: {}, email: {}", memberId, member.getEmail());

        // 4. RefreshToken 삭제
        if (refreshTokenRepository.existsByMemberId(memberId)) {
            refreshTokenRepository.deleteByMemberId(memberId);
            log.info("RefreshToken 삭제 완료 - memberId: {}", memberId);
        }

        // 5. 쿠키 삭제
        cookieUtil.deleteCookie(request, response, "access_token");
        cookieUtil.deleteCookie(request, response, "refresh_token");
        log.info("쿠키 삭제 완료 - memberId: {}", memberId);

        log.info("회원 탈퇴 완료 - memberId: {}", memberId);
    }
}
