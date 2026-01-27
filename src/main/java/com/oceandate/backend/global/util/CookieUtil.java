package com.oceandate.backend.global.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    @Value("${cookie.domain}")
    private String cookieDomain;

    @Value("${cookie.secure-mode}")
    private boolean secureMode;

    public void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(secureMode);
        cookie.setMaxAge(maxAge);

        // 크로스 도메인 환경에서는 domain 설정하지 않음
        if (cookieDomain != null && !cookieDomain.isEmpty()) {
            cookie.setDomain(cookieDomain);
        }

        // SameSite 속성 설정 (크로스 도메인 쿠키 전송을 위해)
        cookie.setAttribute("SameSite", "None");

        response.addCookie(cookie);
    }

    public Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            return Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(name))
                    .findFirst();
        }
        return Optional.empty();
    }

    public void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(name))
                    .forEach(cookie -> {
                        cookie.setValue("");
                        cookie.setPath("/");
                        cookie.setMaxAge(0);
                        response.addCookie(cookie);
                    });
        }
    }
}
