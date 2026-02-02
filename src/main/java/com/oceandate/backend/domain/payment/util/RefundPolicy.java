package com.oceandate.backend.domain.payment.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RefundPolicy {

    // 로테이션 환불 정책 상수
    private static final int ROTATION_FULL_REFUND_DAYS = 7;
    private static final int ROTATION_PARTIAL_REFUND_DAYS = 3;
    private static final int ROTATION_PARTIAL_REFUND_RATE = 50;

    // 일대일 환불 정책 상수
    private static final int ONE_TO_ONE_FULL_REFUND_HOURS = 24;
    private static final int ONE_TO_ONE_PARTIAL_REFUND_RATE = 50;

    /**
     * 로테이션 소개팅 환불 금액 계산
     */
    public static RefundAmount calculateRotationRefund(
            LocalDateTime eventDateTime,
            LocalDateTime cancelDateTime,
            int paidAmount
    ) {
        long daysUntilEvent = ChronoUnit.DAYS.between(cancelDateTime, eventDateTime);

        if (daysUntilEvent >= ROTATION_FULL_REFUND_DAYS) {
            return new RefundAmount(
                    paidAmount,
                    100,
                    String.format("이벤트 %d일 전 취소 (전액 환불)", ROTATION_FULL_REFUND_DAYS)
            );
        } else if (daysUntilEvent >= ROTATION_PARTIAL_REFUND_DAYS) {
            int refundAmount = paidAmount * ROTATION_PARTIAL_REFUND_RATE / 100;
            return new RefundAmount(
                    refundAmount,
                    ROTATION_PARTIAL_REFUND_RATE,
                    String.format("이벤트 %d~%d일 전 취소 (%d%% 환불)",
                            ROTATION_PARTIAL_REFUND_DAYS,
                            ROTATION_FULL_REFUND_DAYS - 1,
                            ROTATION_PARTIAL_REFUND_RATE)
            );
        } else {
            return new RefundAmount(
                    0,
                    0,
                    String.format("이벤트 %d일 전 이후 취소 (환불 불가)", ROTATION_PARTIAL_REFUND_DAYS)
            );
        }
    }

    /**
     * 일대일 소개팅 환불 금액 계산
     */
    public static RefundAmount calculateOneToOneRefund(
            LocalDateTime matchedDateTime,
            LocalDateTime cancelDateTime,
            int paidAmount
    ) {
        long hoursAfterMatch = ChronoUnit.HOURS.between(matchedDateTime, cancelDateTime);

        if (hoursAfterMatch <= ONE_TO_ONE_FULL_REFUND_HOURS) {
            return new RefundAmount(
                    paidAmount,
                    100,
                    String.format("매칭 후 %d시간 이내 취소 (전액 환불)", ONE_TO_ONE_FULL_REFUND_HOURS)
            );
        } else {
            int refundAmount = paidAmount * ONE_TO_ONE_PARTIAL_REFUND_RATE / 100;
            return new RefundAmount(
                    refundAmount,
                    ONE_TO_ONE_PARTIAL_REFUND_RATE,
                    String.format("매칭 후 %d시간 경과 (%d%% 환불)",
                            ONE_TO_ONE_FULL_REFUND_HOURS,
                            ONE_TO_ONE_PARTIAL_REFUND_RATE)
            );
        }
    }

    @Getter
    @AllArgsConstructor
    public static class RefundAmount {
        private int amount;      // 환불 금액
        private int rate;        // 환불 비율 (%)
        private String reason;   // 환불 사유
    }
}