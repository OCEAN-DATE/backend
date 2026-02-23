package com.oceandate.backend.domain.payment.util;

import com.oceandate.backend.global.exception.constant.ErrorCode;

import java.util.Map;

public class PortOneErrorMapper {
    private static final Map<String, ErrorCode> ERROR_CODE_MAP = Map.ofEntries(
            // 400
            Map.entry("INVALID_REQUEST", ErrorCode.INVALID_AUTHORIZE_AUTH),
            // 401
            Map.entry("UNAUTHORIZED", ErrorCode.FORBIDDEN_REQUEST),
            // 403
            Map.entry("FORBIDDEN", ErrorCode.FORBIDDEN_REQUEST),
            // 404
            Map.entry("PAYMENT_NOT_FOUND", ErrorCode.NOT_FOUND_PAYMENT),
            // 409
            Map.entry("PAYMENT_NOT_PAID", ErrorCode.NOT_AVAILABLE_PAYMENT),
            Map.entry("PAYMENT_ALREADY_CANCELLED", ErrorCode.ALREADY_PROCESSED_PAYMENT),
            Map.entry("CANCEL_AMOUNT_EXCEEDS_CANCELLABLE_AMOUNT", ErrorCode.EXCEED_MAX_AMOUNT),
            Map.entry("CANCELLABLE_AMOUNT_CONSISTENCY_BROKEN", ErrorCode.FAILED_INTERNAL_SYSTEM_PROCESSING),
            Map.entry("SUM_OF_PARTS_EXCEEDS_CANCEL_AMOUNT", ErrorCode.FAILED_INTERNAL_SYSTEM_PROCESSING),
            Map.entry("CANCEL_TAX_AMOUNT_EXCEEDS_CANCELLABLE_TAX_AMOUNT", ErrorCode.FAILED_INTERNAL_SYSTEM_PROCESSING),
            Map.entry("CANCEL_TAX_FREE_AMOUNT_EXCEEDS_CANCELLABLE_TAX_FREE_AMOUNT", ErrorCode.FAILED_INTERNAL_SYSTEM_PROCESSING),
            // 502
            Map.entry("PG_PROVIDER", ErrorCode.PROVIDER_ERROR)
    );

    public static ErrorCode fromPortOneErrorType(String type) {
        return ERROR_CODE_MAP.getOrDefault(type, ErrorCode.UNKNOWN_PAYMENT_ERROR);
    }
}
