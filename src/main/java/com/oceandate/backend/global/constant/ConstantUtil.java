package com.oceandate.backend.global.constant;

import static lombok.AccessLevel.PRIVATE;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public class ConstantUtil {

    // Header Constants
    public static final String TOKEN_HEADER = "Authorization";

    // Bearer Token Prefix
    public static final String BEARER_PREFIX = "Bearer ";

    // Cookie Name
    public static final String COOKIE_NAME = "refresh_token";

    // Login Redirects
    public static final String LOGIN_REDIRECT_URL = "/login/callback";
    public static final String ERROR_REDIRECT_URL = "/login?error=";

    // User Info URL
    public static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v1/oidc/userinfo";

    // JWT Token Expiration
    public static final int ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 3; // 3 hours
    public static final int REFRESH_TOKEN_EXPIRE_TIME = 1000 * 60 * 60 * 24 * 7; // 7 days
}
