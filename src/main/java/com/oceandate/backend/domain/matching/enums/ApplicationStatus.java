package com.oceandate.backend.domain.matching.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
    APPLICATION_SUBMITTED(true, false, false, "신청접수"),
    UNDER_REVIEW(true, false, false, "검토중"),
    APPROVED(true, false, false, "승인완료"),
    MATCHED(true, false, true, "매칭완료"),
    PAYMENT_PENDING(true, false, true, "결제대기"),
    PAYMENT_COMPLETED(true, true, true, "결제완료"),
    CANCEL_REQUESTED(false, false, true, "취소요청중"),
    CANCELLED(false, false, false, "취소됨"),
    PARTNER_CANCELLED_BEFORE_PAYMENT(true, false, false, "상대방취소(결제전)"),
    PARTNER_CANCELLED_AFTER_PAYMENT(false, true, false, "상대방취소(결제후)"),
    COMPLETED(false, false, false, "이용완료"),
    NO_SHOW(false, false, false, "노쇼");

    private final boolean cancellable;
    private final boolean refundRequired;
    private final boolean requiresAdminCancel;
    private final String description;

    public boolean isBeforeMatched() {
        return this == APPLICATION_SUBMITTED
                || this == UNDER_REVIEW
                || this == APPROVED;
    }
}