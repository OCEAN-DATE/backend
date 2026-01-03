package com.oceandate.backend.domain.user.service;

import com.oceandate.backend.domain.user.dto.response.IdentifierResponse;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.entity.RefreshToken;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.domain.user.repository.RefreshTokenRepository;
import com.oceandate.backend.global.jwt.AccountContext;
import com.oceandate.backend.global.jwt.AuthToken;
import com.oceandate.backend.global.jwt.JwtTokenProvider;
import com.oceandate.backend.global.jwt.JwtTokenValidator;
import com.oceandate.backend.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.oceandate.backend.global.jwt.AuthTokenType.ACCESS;
import static com.oceandate.backend.global.jwt.AuthTokenType.REFRESH;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TokenService {

    private final CookieUtil cookieUtil;
    private final MemberRepository memberRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtTokenValidator jwtTokenValidator;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final String COOKIE_NAME = "refresh_token";
    private static final int REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7;

    @Transactional
    public IdentifierResponse reissueToken(HttpServletRequest request, HttpServletResponse response) {

        // 쿠키에서 Refresh Token 가져오기
        String refreshToken = cookieUtil.getCookie(request, COOKIE_NAME)
                .map(Cookie::getValue)
                .orElseThrow(() -> new IllegalArgumentException("Refresh Token이 없습니다."));

        // Refresh Token 검증
        if (!jwtTokenValidator.validateToken(refreshToken)) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        // Authentication 생성
        Authentication authentication = jwtTokenValidator.getAuthentication(refreshToken);
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();

        // 회원 조회
        Member member = memberRepository.findById(accountContext.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원입니다."));

        // DB의 Refresh Token과 비교
        RefreshToken savedRefreshToken = refreshTokenRepository.findById(member.getId())
                .orElseThrow(() -> new IllegalArgumentException("저장된 Refresh Token이 없습니다."));

        if (!refreshToken.equals(savedRefreshToken.getToken())) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        // 새로운 Refresh Token 발급
        AuthToken newRefreshToken = jwtTokenProvider.createToken(authentication, REFRESH);
        refreshTokenRepository.save(RefreshToken.of(member.getId(), newRefreshToken.token()));

        // 쿠키 갱신
        cookieUtil.deleteCookie(request, response, COOKIE_NAME);
        cookieUtil.addCookie(response, COOKIE_NAME, newRefreshToken.token(), REFRESH_TOKEN_EXPIRE_TIME);

        // Access Token 발급
        AuthToken accessToken = jwtTokenProvider.createToken(authentication, ACCESS);
        response.setHeader("Authorization", "Bearer " + accessToken.token());

        return IdentifierResponse.of(member.getId(), member.getName());
    }
}
