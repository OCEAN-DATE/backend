package com.oceandate.backend.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.List;

import static io.jsonwebtoken.io.Decoders.BASE64;

@Component
public class JwtTokenValidator {

    private final Key key;

    public JwtTokenValidator(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Authentication getAuthentication(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        Long memberId = claims.get("memberId", Long.class);
        @SuppressWarnings("unchecked")
        List<String> authorities = claims.get("authorities", List.class);

        List<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();

        AccountContext accountContext = AccountContext.of(memberId, grantedAuthorities);

        return new UsernamePasswordAuthenticationToken(accountContext, token, grantedAuthorities);
    }
}
