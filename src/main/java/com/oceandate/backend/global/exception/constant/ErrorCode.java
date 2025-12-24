package com.oceandate.backend.global.exception.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 공통
    INVALID_INPUT("COMMON_400", "잘못된 입력입니다."),
    UNAUTHORIZED("COMMON_401", "인증이 필요합니다."),
    ACCESS_DENIED("COMMON_403", "접근 권한이 없습니다."),
    NOT_FOUND("COMMON_404", "리소스를 찾을 수 없습니다."),
    DUPLICATE_RESOURCE("COMMON_409", "이미 존재하는 리소스입니다."),
    INTERNAL_ERROR("COMMON_500", "서버 내부 오류가 발생했습니다."),

    // 사용자
    USER_NOT_FOUND("USER_404", "사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL("USER_409", "이미 사용 중인 이메일입니다."),

    // 로테이션 이벤트
    EVENT_NOT_FOUND("EVENT_404", "이벤트를 찾을 수 없습니다."),
    EVENT_CLOSED("EVENT_400", "모집이 종료된 이벤트입니다."),
    MALE_CAPACITY_FULL("EVENT_400", "남성 정원이 마감되었습니다."),
    FEMALE_CAPACITY_FULL("EVENT_400", "여성 정원이 마감되었습니다."),
    EVENT_FULL("EVENT_400", "정원이 마감되었습니다."),

    // 로테이션 신청
    APPLICATION_NOT_FOUND("APPLICATION_404", "신청을 찾을 수 없습니다."),
    DUPLICATE_APPLICATION("APPLICATION_409", "이미 신청한 이벤트입니다."),
    INVALID_APPLICATION_STATUS("APPLICATION_400", "잘못된 신청 상태입니다."),
    ONLY_PENDING_CAN_APPROVE("APPLICATION_400", "대기 중인 신청만 승인할 수 있습니다."),
    ONLY_PENDING_CAN_REJECT("APPLICATION_400", "대기 중인 신청만 반려할 수 있습니다."),
    ONLY_APPROVED_CAN_CANCEL("APPLICATION_400", "승인된 신청만 취소할 수 있습니다."),
    ONLY_APPROVED_CAN_CHECKIN("APPLICATION_400", "승인된 참가자만 체크인할 수 있습니다."),
    CHECKIN_REQUIRED_FOR_INTEREST("APPLICATION_400", "체크인을 완료해야 관심 표시가 가능합니다."),
    DOCUMENT_DEADLINE_PASSED("APPLICATION_400", "서류 제출 기한이 지났습니다."),
    DOCUMENT_ALREADY_SUBMITTED("APPLICATION_400", "이미 서류를 제출했습니다."),

    // 결제
    PAYMENT_FAILED("PAYMENT_400", "결제에 실패했습니다."),
    REFUND_FAILED("PAYMENT_400", "환불에 실패했습니다."),

    // 파일
    FILE_UPLOAD_FAILED("FILE_500", "파일 업로드에 실패했습니다.");

    private final String code;
    private final String message;
}
