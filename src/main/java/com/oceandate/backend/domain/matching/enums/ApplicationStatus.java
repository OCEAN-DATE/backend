package com.oceandate.backend.domain.matching.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
    APPLICATION_SUBMITTED(true, false, "신청접수"),
    UNDER_REVIEW(true, false, "검토중"),
    APPROVED(true, false, "승인완료"),
    MATCHED(true, false, "매칭완료"),
    PAYMENT_PENDING(true, false, "결제대기"),
    PAYMENT_COMPLETED(true, true, "결제완료"),
    CANCELLED(false, false, "취소됨"),
    COMPLETED(false, false, "이용완료"),
    NO_SHOW(false, false, "노쇼");

    private final boolean cancellable;      // 취소 가능 여부
    private final boolean refundRequired;   // 환불 필요 여부
    private final String description;
}