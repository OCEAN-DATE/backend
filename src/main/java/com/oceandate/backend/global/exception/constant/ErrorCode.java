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

    //매칭
    GENDER_MISMATCH("MATCHING_400", "신청서의 성별이 올바르지 않습니다."),
    DIFFERENT_EVENT_APPLICATION("MATCHING_400", "다른 이벤트의 신청서입니다."),
    ALREADY_MATCHED("MATCHING_409", "이미 매칭된 신청서입니다."),
    INVALID_MATCHING_STATUS("MATCHING_400", "매칭 가능한 상태가 아닙니다."),

    // 이벤트
    EVENT_NOT_FOUND("EVENT_404", "이벤트를 찾을 수 없습니다."),
    EVENT_CLOSED("EVENT_400", "모집이 종료된 이벤트입니다."),
    MALE_CAPACITY_FULL("EVENT_400", "남성 정원이 마감되었습니다."),
    FEMALE_CAPACITY_FULL("EVENT_400", "여성 정원이 마감되었습니다."),
    EVENT_FULL("EVENT_400", "정원이 마감되었습니다."),

    // 신청
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
    INVALID_MATCHING_TYPE("PAYMENT_400", "매칭 유형이 유효하지 않습니다." ),
    PAYMENT_FAILED("PAYMENT_400", "결제에 실패했습니다."),
    REFUND_FAILED("PAYMENT_400", "환불에 실패했습니다."),
    ORDER_NOT_FOUND("ORDER_404", "주문을 찾을 수 없습니다."),
    INVALID_PAYMENT_STATUS("PAYMENT_400", "결제 가능한 상태가 아닙니다."),
    INVALID_CANCEL_STATUS("PAYMENT_400", "환불 가능한 상태가 아닙니다."),
    PAYMENT_AMOUNT_MISMATCH("PAYMENT_400", "결제 금액이 일치하지 않습니다."),
    PAYMENT_CONFIRMATION_FAILED("PAYMENT_400", "결제 승인에 실패했습니다."),
    PAYMENT_PROCESSING("PAYMENT_400", "결제가 진행 중입니다."),
    PAYMENT_KEY_MISMATCH("PAYMENT_400", "결제 키가 일치하지 않습니다."),
    PAYMENT_DB_SAVE_FAILED("PAYMENT_400", "결제는 완료되었으나 DB 저장에 실패했습니다. 자동 취소를 시도합니다."),

    //토스
    ALREADY_PROCESSED_PAYMENT("TOSS_001", "이미 처리된 결제입니다"),
    PROVIDER_ERROR("TOSS_002", "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요"),
    EXCEED_MAX_DAILY_PAYMENT_COUNT("TOSS_003", "하루 결제 가능 횟수를 초과했습니다"),
    NOT_AVAILABLE_BANK("TOSS_004", "은행 서비스 시간이 아닙니다"),
    EXCEED_MAX_AMOUNT("TOSS_005", "결제 가능 금액을 초과했습니다"),
    INVALID_CARD_EXPIRATION("TOSS_006", "카드 유효기간이 만료되었습니다"),
    INVALID_STOPPED_CARD("TOSS_007", "정지된 카드입니다"),
    EXCEED_MAX_CARD_MONTHLY_AMOUNT("TOSS_008", "월 한도를 초과했습니다"),
    INVALID_CARD_INSTALLMENT_PLAN("TOSS_009", "할부가 불가능한 카드입니다"),
    NOT_SUPPORTED_INSTALLMENT_PLAN_MERCHANT("TOSS_010", "할부가 불가능한 가맹점입니다"),
    INVALID_AUTHORIZE_AUTH("TOSS_011", "카드 정보가 정확하지 않습니다"),
    INVALID_CARD_LOST_OR_STOLEN("TOSS_012", "분실 또는 도난 카드입니다"),
    RESTRICTED_TRANSFER_ACCOUNT("TOSS_013", "계좌가 정지 상태입니다"),
    INVALID_ACCOUNT_INFO_RE_REGISTER("TOSS_014", "계좌 정보가 정확하지 않습니다"),
    NOT_AVAILABLE_PAYMENT("TOSS_015", "결제가 불가능한 시간대입니다"),
    UNAPPROVED_ORDER_ID("TOSS_016", "아직 승인되지 않은 주문입니다"),
    REJECT_CARD_PAYMENT("TOSS_017", "카드사에서 승인이 거절되었습니다"),
    REJECT_CARD_COMPANY("TOSS_018", "카드사에서 승인이 거절되었습니다"),
    FORBIDDEN_REQUEST("TOSS_019", "허용되지 않은 요청입니다"),
    REJECT_TOSSPAY_INVALID_ACCOUNT("TOSS_020", "선택하신 출금 계좌가 등록되어 있지 않습니다"),
    EXCEED_MAX_WEEKLY_PAYMENT_COUNT("TOSS_021", "주간 결제 한도를 초과했습니다"),
    EXCEED_MAX_WEEKLY_PAYMENT_AMOUNT("TOSS_022", "주간 결제 금액을 초과했습니다"),
    EXCEED_MAX_MONTHLY_PAYMENT_COUNT("TOSS_023", "월간 결제 한도를 초과했습니다"),
    NOT_FOUND_PAYMENT_SESSION( "TOSS_024", "결제 시간이 만료되었습니다"),
    NOT_FOUND_PAYMENT("TOSS_025", "결제 정보를 찾을 수 없습니다"),
    FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING("TOSS_026", "결제가 완료되지 않았습니다. 다시 시도해주세요"),
    FAILED_INTERNAL_SYSTEM_PROCESSING("TOSS_027", "내부 시스템 오류가 발생했습니다"),
    UNKNOWN_PAYMENT_ERROR("TOSS_028", "알 수 없는 결제 오류가 발생했습니다"),

    // 파일
    FILE_UPLOAD_FAILED("FILE_500", "파일 업로드에 실패했습니다.");

    private final String code;
    private final String message;
}
