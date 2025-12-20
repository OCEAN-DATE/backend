package com.oceandate.backend.global.exception.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_INPUT("COMMON_400", "잘못된 입력입니다."),
    UNAUTHORIZED("COMMON_401", "인증이 필요합니다."),
    ACCESS_DENIED("COMMON_403", "접근 권한이 없습니다."),
    NOT_FOUND("COMMON_404", "리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE("COMMON_409", "이미 존재하는 리소스입니다."),
    INTERNAL_ERROR("COMMON_500", "서버 내부 오류가 발생했습니다."),

    // 사용자
    USER_NOT_FOUND("USER_404", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL("USER_409", "이미 사용 중인 이메일입니다.");

    private final String code;
    private final String message;
}
