package com.oceandate.backend.domain.notification.entity;

public enum NotificationType {

    // ===== PAYMENT =====
    PAYMENT_COMPLETED,          // 결제 완료
    PAYMENT_FAILED,             // 결제 실패(결제 오류/승인 실패)
    PAYMENT_REFUNDED,           // 환불 완료
    PAYMENT_CANCELED,           // 결제 취소(승인 취소)

    // ===== RESERVATION (펜션/예약) =====
    RESERVATION_CREATED,        // 예약 접수/생성
    RESERVATION_CONFIRMED,      // 예약 확정
    RESERVATION_CANCELED,       // 예약 취소
    RESERVATION_CHANGED,        // 예약 변경(날짜/인원 등)
    RESERVATION_REMINDER_D1,    // 이용 전날 안내
    RESERVATION_REMINDER_D0,    // 이용 당일 안내(옵션)
    RESERVATION_CHECKIN_GUIDE,  // 체크인 가이드(주소/비밀번호/주의사항 등)
    RESERVATION_REVIEW_REQUEST, // 이용 후 리뷰 요청(마케팅 성격이면 별도 동의)

    // ===== DATING / SESSION (소개팅/매칭) =====
    DATING_REQUESTED,           // 매칭/소개팅 신청
    DATING_CONFIRMED,           // 일정 확정
    DATING_CANCELED,            // 취소
    DATING_REMINDER_D1,         // 전날 리마인드
    DATING_REMINDER_D0,         // 당일 리마인드

    // ===== SYSTEM / ACCOUNT =====
    ACCOUNT_WELCOME,            // 가입 환영(정보성으로만)
    PASSWORD_RESET,             // 비밀번호 재설정
    SECURITY_ALERT              // 보안 알림(로그인/인증 등)
}
