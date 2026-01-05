package com.oceandate.backend.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

import static io.jsonwebtoken.Header.JWT_TYPE;
import static io.jsonwebtoken.Header.TYPE;
import static io.jsonwebtoken.SignatureAlgorithm.HS256;
import static io.jsonwebtoken.io.Decoders.BASE64;

@Component
public class JwtTokenProvider {

    private final Key key;
    private static final int ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 3; // 3시간
    private static final int REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7일

    public JwtTokenProvider(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public AuthToken createToken(Authentication authentication, AuthTokenType tokenType) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();

        Date now = new Date();
        int expiration = tokenType == AuthTokenType.ACCESS ? ACCESS_TOKEN_EXPIRE_TIME : REFRESH_TOKEN_EXPIRE_TIME;
        Date expiryDate = new Date(now.getTime() + expiration);

        List<String> authorities = accountContext.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        String jwtToken = Jwts.builder()
                .setHeaderParam(TYPE, JWT_TYPE)
                .setSubject("AUTH")
                .claim("memberId", accountContext.getMemberId())
                .claim("authorities", authorities)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, HS256)
                .compact();

        return AuthToken.builder()
                .memberId(accountContext.getMemberId())
                .token(jwtToken)
                .expiresIn(expiration)
                .type(tokenType)
                .build();
    }
}
