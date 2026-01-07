package com.oceandate.backend.global.response;

import com.oceandate.backend.global.exception.constant.ErrorCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final String errorCode;
    private final String message;
    private final LocalDateTime timestamp;

    // ===== 성공 응답 =====

    // 데이터만
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null, null, LocalDateTime.now());
    }

    // 메시지 + 데이터
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, data, null, message, LocalDateTime.now());
    }

    // 데이터 없이 성공만
    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, null, null, null, LocalDateTime.now());
    }

    // ===== 에러 응답 =====

    // ErrorCode만 (기본 메시지 사용)
    public static <T> ApiResponse<T> error(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode.getCode(),
                errorCode.getMessage(), LocalDateTime.now());
    }

    // ErrorCode + 커스텀 메시지
    public static <T> ApiResponse<T> error(ErrorCode errorCode, String customMessage) {
        return new ApiResponse<>(false, null, errorCode.getCode(),
                customMessage, LocalDateTime.now());
    }
}