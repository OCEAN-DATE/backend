package com.oceandate.backend.global.oauth2.handler;

import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.entity.RefreshToken;
import com.oceandate.backend.domain.user.repository.RefreshTokenRepository;
import com.oceandate.backend.domain.user.service.MemberService;
import com.oceandate.backend.global.jwt.AccountContext;
import com.oceandate.backend.global.jwt.AuthToken;
import com.oceandate.backend.global.jwt.AuthTokenType;
import com.oceandate.backend.global.jwt.JwtTokenProvider;
import com.oceandate.backend.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import java.io.IOException;

import static com.oceandate.backend.global.jwt.AuthTokenType.REFRESH;


@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Value("${host.frontend}")
    private String frontendUrl;

    private final CookieUtil cookieUtil;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String COOKIE_NAME = "refresh_token";
    private static final int REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        log.info("OAuth2 로그인 성공");

        AccountContext accountContext = (AccountContext) authentication.getPrincipal();

        // 회원 등록 또는 조회
        Member member = memberService.registerMember(accountContext.getSocialUserInfo());
        accountContext.updateMemberInfo(member);

        // ⭐ 1. AccessToken 생성 추가
        AuthToken accessToken = jwtTokenProvider.createToken(authentication, AuthTokenType.ACCESS);
        log.info("✅ AccessToken 생성 완료: {}", accessToken.token().substring(0, 20) + "...");

        // 2. Refresh Token 생성 및 쿠키 설정
        setRefreshTokenCookie(authentication, response);

        // ⭐ 3. 프론트엔드로 리다이렉트 (AccessToken 포함)
        String redirectUrl = UriComponentsBuilder
                .fromUriString(frontendUrl + "/login/callback")
                .queryParam("accessToken", accessToken.token())
                .build()
                .toUriString();

        log.info("리다이렉트 URL: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }

    private void setRefreshTokenCookie(Authentication authentication, HttpServletResponse response) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();

        // Refresh Token 생성
        AuthToken refreshToken = jwtTokenProvider.createToken(authentication, REFRESH);

        // DB에 저장
        refreshTokenRepository.save(RefreshToken.of(accountContext.getMemberId(), refreshToken.token()));

        // 쿠키에 담기
        cookieUtil.addCookie(response, COOKIE_NAME, refreshToken.token(), REFRESH_TOKEN_EXPIRE_TIME);
    }
}
