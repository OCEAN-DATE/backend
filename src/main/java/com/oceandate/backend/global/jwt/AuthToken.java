package com.oceandate.backend.global.jwt;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AuthToken {
    private Long memberId;
    private String token;
    private int expiresIn;
    private AuthTokenType type;

    public String token() {
        return token;
    }
}
