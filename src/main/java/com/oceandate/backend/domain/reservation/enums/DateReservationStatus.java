package com.oceandate.backend.domain.reservation.enums;

public enum DateReservationStatus {
    APPLICATION_SUBMITTED,  // 신청접수
    UNDER_REVIEW,          // 검토중
    APPROVED,              // 승인완료
    PAYMENT_PENDING,       // 결제대기
    PAYMENT_COMPLETED,     // 결제완료
    CANCELLED,             // 취소됨
    COMPLETED              // 이용완료
}
