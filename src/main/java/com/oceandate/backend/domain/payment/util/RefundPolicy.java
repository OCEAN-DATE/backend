package com.oceandate.backend.domain.payment.util;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class RefundPolicy {

    private static final int FULL_REFUND_DAYS = 5;       // 5일 전까지 전액 (결제 당일) or 80%
    private static final int PARTIAL_50_DAYS = 3;        // 3일 전 50%
    private static final int NO_REFUND_DAYS = 2;         // 2일 전 환불 불가

    /**
     * 공통 환불 금액 계산 (로테이션 / 일대일 모두 사용)
     *
     * @param eventDate     모임 날짜 (confirmedDate)
     * @param paymentDate   결제 승인 날짜
     * @param cancelDateTime 취소 시각
     * @param paidAmount    결제 금액
     */
    public static RefundAmount calculate(
            LocalDateTime eventDate,
            LocalDateTime paymentDate,
            LocalDateTime cancelDateTime,
            int paidAmount
    ) {
        LocalDate eventDay = eventDate.toLocalDate();
        LocalDate cancelDay = cancelDateTime.toLocalDate();
        LocalDate paymentDay = paymentDate.toLocalDate();

        long daysUntilEvent = ChronoUnit.DAYS.between(cancelDay, eventDay);

        // 모임 2일 전 이후 → 환불 불가
        if (daysUntilEvent < NO_REFUND_DAYS) {
            return new RefundAmount(0, 0,
                    String.format("모임 %d일 전 이후 취소 (환불 불가)", NO_REFUND_DAYS));
        }

        // 모임 3일 전 → 50%
        if (daysUntilEvent < PARTIAL_50_DAYS) {
            int refund = paidAmount * 50 / 100;
            return new RefundAmount(refund, 50,
                    String.format("모임 %d일 전 취소 (50%% 환불)", PARTIAL_50_DAYS));
        }

        // 모임 5일+ 전이고 결제 당일 → 100%
        if (daysUntilEvent >= FULL_REFUND_DAYS && cancelDay.isEqual(paymentDay)) {
            return new RefundAmount(paidAmount, 100, "결제 당일 취소 (전액 환불)");
        }

        // 그 외 (모임 4~5일+ 전, 결제 다음날 이후) → 80%
        int refund = paidAmount * 80 / 100;
        return new RefundAmount(refund, 80,
                String.format("모임 %d일 전 취소 (80%% 환불)", daysUntilEvent));
    }

    /**
     * 상대방 강제 취소 시 전액 환불
     */
    public static RefundAmount fullRefundByCounterpart(int paidAmount) {
        return new RefundAmount(paidAmount, 100, "상대방 취소로 인한 전액 환불");
    }

    @Getter
    @AllArgsConstructor
    public static class RefundAmount {
        private int amount;
        private int rate;
        private String reason;
    }
}