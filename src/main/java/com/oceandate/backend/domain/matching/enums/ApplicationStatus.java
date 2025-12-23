package com.oceandate.backend.domain.matching.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ApplicationStatus {
    APPLICATION_SUBMITTED,  // 신청접수
    UNDER_REVIEW,          // 검토중
    APPROVED,              // 승인완료
    PAYMENT_PENDING,       // 결제대기
    PAYMENT_COMPLETED,     // 결제완료
    CANCELLED,             // 취소됨
    COMPLETED              // 이용완료
}
