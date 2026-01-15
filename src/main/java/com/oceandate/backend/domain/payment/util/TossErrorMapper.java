package com.oceandate.backend.domain.payment.util;

import com.oceandate.backend.global.exception.constant.ErrorCode;

import java.util.Map;

public class TossErrorMapper {
    private static final Map<String, ErrorCode> ERROR_CODE_MAP = Map.ofEntries(
            Map.entry("ALREADY_PROCESSED_PAYMENT", ErrorCode.ALREADY_PROCESSED_PAYMENT),
            Map.entry("PROVIDER_ERROR", ErrorCode.PROVIDER_ERROR),
            Map.entry("EXCEED_MAX_DAILY_PAYMENT_COUNT", ErrorCode.EXCEED_MAX_DAILY_PAYMENT_COUNT),
            Map.entry("NOT_AVAILABLE_BANK", ErrorCode.NOT_AVAILABLE_BANK),
            Map.entry("EXCEED_MAX_AMOUNT", ErrorCode.EXCEED_MAX_AMOUNT),
            Map.entry("INVALID_CARD_EXPIRATION", ErrorCode.INVALID_CARD_EXPIRATION),
            Map.entry("INVALID_STOPPED_CARD", ErrorCode.INVALID_STOPPED_CARD),
            Map.entry("EXCEED_MAX_CARD_MONTHLY_AMOUNT", ErrorCode.EXCEED_MAX_CARD_MONTHLY_AMOUNT),
            Map.entry("INVALID_CARD_INSTALLMENT_PLAN", ErrorCode.INVALID_CARD_INSTALLMENT_PLAN),
            Map.entry("NOT_SUPPORTED_INSTALLMENT_PLAN_MERCHANT", ErrorCode.NOT_SUPPORTED_INSTALLMENT_PLAN_MERCHANT),
            Map.entry("INVALID_AUTHORIZE_AUTH", ErrorCode.INVALID_AUTHORIZE_AUTH),
            Map.entry("INVALID_CARD_LOST_OR_STOLEN", ErrorCode.INVALID_CARD_LOST_OR_STOLEN),
            Map.entry("RESTRICTED_TRANSFER_ACCOUNT", ErrorCode.RESTRICTED_TRANSFER_ACCOUNT),
            Map.entry("INVALID_ACCOUNT_INFO_RE_REGISTER", ErrorCode.INVALID_ACCOUNT_INFO_RE_REGISTER),
            Map.entry("NOT_AVAILABLE_PAYMENT", ErrorCode.NOT_AVAILABLE_PAYMENT),
            Map.entry("UNAPPROVED_ORDER_ID", ErrorCode.UNAPPROVED_ORDER_ID),
            Map.entry("REJECT_CARD_PAYMENT", ErrorCode.REJECT_CARD_PAYMENT),
            Map.entry("REJECT_CARD_COMPANY", ErrorCode.REJECT_CARD_COMPANY),
            Map.entry("FORBIDDEN_REQUEST", ErrorCode.FORBIDDEN_REQUEST),
            Map.entry("REJECT_TOSSPAY_INVALID_ACCOUNT", ErrorCode.REJECT_TOSSPAY_INVALID_ACCOUNT),
            Map.entry("EXCEED_MAX_WEEKLY_PAYMENT_COUNT", ErrorCode.EXCEED_MAX_WEEKLY_PAYMENT_COUNT),
            Map.entry("EXCEED_MAX_WEEKLY_PAYMENT_AMOUNT", ErrorCode.EXCEED_MAX_WEEKLY_PAYMENT_AMOUNT),
            Map.entry("EXCEED_MAX_MONTHLY_PAYMENT_COUNT", ErrorCode.EXCEED_MAX_MONTHLY_PAYMENT_COUNT),
            Map.entry("NOT_FOUND_PAYMENT_SESSION", ErrorCode.NOT_FOUND_PAYMENT_SESSION),
            Map.entry("NOT_FOUND_PAYMENT", ErrorCode.NOT_FOUND_PAYMENT),
            Map.entry("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", ErrorCode.FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING),
            Map.entry("FAILED_INTERNAL_SYSTEM_PROCESSING", ErrorCode.FAILED_INTERNAL_SYSTEM_PROCESSING)
    );

    public static ErrorCode fromTossErrorCode(String tossErrorCode) {
        return ERROR_CODE_MAP.getOrDefault(tossErrorCode, ErrorCode.UNKNOWN_PAYMENT_ERROR);
    }
}
