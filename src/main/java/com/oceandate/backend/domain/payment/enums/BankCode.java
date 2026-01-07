package com.oceandate.backend.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BankCode {

    KYONGNAMBANK("39", "경남은행"),
    GWANGJUBANK("34", "광주은행"),
    LOCALNONGHYEOP("12", "지역농축협"),
    BUSANBANK("32", "부산은행"),
    SAEMAUL("45", "새마을금고"),
    SANLIM("64", "산림조합"),
    SHINHAN("88", "신한은행"),
    SHINHYEOP("48", "신협"),
    CITI("27", "씨티은행"),
    WOORI("20", "우리은행"),
    POST("71", "우체국"),
    SAVINGBANK("50", "저축"),
    JEONBUKBANK("37", "전북은행"),
    JEJUBANK("35", "제주은행"),
    KAKAOBANK("90", "카카오뱅크"),
    KBANK("89", "케이뱅크"),
    TOSSBANK("92", "토스뱅크"),
    HANA("81", "하나은행"),
    HSBC("54", "HSBC은행"),
    IBK("03", "기업은행"),
    KOOKMIN("04", "국민은행"),
    DAEGUBANK("31", "대구은행"),
    KDBBANK("02", "산업은행"),
    NONGHYEOP("11", "농협은행"),
    SC("23", "SC제일은행"),
    SUHYEOP("07", "수협은행"),
    SUHYEOPLOCALBANK("30", "수협중앙회"),

    // 증권사
    KYOBO_SECURITIES("S8", "교보증권"),
    DAISHIN_SECURITIES("SE", "대신증권"),
    MERITZ_SECURITIES("SK", "메리츠증권"),
    MIRAE_ASSET_SECURITIES("S5", "미래에셋증권"),
    BOOKOOK_SECURITIES("S3", "삼성증권"),
    SHINYOUNG_SECURITIES("SN", "신영증권"),
    SHINHAN_SECURITIES("S2", "신한금융투자"),
    YUANYA_SECURITIES("S0", "유안타증권"),
    EUGENE_INVESTMENT_AND_SECURITIES("SJ", "유진투자증권"),
    KAKAOPAY_SECURITIES("SQ", "카카오페이증권"),
    KIWOOM("SB", "키움증권"),
    TOSS_SECURITIES("ST", "토스증권"),
    KOREA_FOSS_SECURITIES("SR", "펀드온라인코리아"),
    HANA_INVESTMENT_AND_SECURITIES("SH", "하나금융투자"),
    HI_INVESTMENT_AND_SECURITIES("S9", "하이투자증권"),
    KOREA_INVESTMENT_AND_SECURITIES("S6", "한국투자증권"),
    HANHWA_INVESTMENT_AND_SECURITIES("SG", "한화투자증권"),
    HYUNDAI_MOTOR_SECURITIES("SA", "현대차증권"),
    DB_INVESTMENT_AND_SECURITIES("SI", "DB금융투자"),
    KB_SECURITIES("S4", "KB증권"),
    DAOL_INVESTMENT_AND_SECURITIES("SP", "KTB투자증권"),
    LIG_INVESTMENT_AND_SECURITIES("SO", "LIG투자"),
    NH_INVESTMENT_AND_SECURITIES("SL", "NH투자증권"),
    SK_SECURITIES("SD", "SK증권"),

    // 기타
    UNKNOWN("99", "알 수 없음");

    private final String code;
    private final String bankName;

    /**
     * 은행 코드로 Enum 찾기
     */
    public static BankCode fromCode(String code) {
        for (BankCode bank : values()) {
            if (bank.code.equals(code)) {
                return bank;
            }
        }
        return UNKNOWN;  // 알 수 없는 코드는 UNKNOWN 반환
    }

    /**
     * 은행명으로 Enum 찾기
     */
    public static BankCode fromBankName(String bankName) {
        for (BankCode bank : values()) {
            if (bank.bankName.equals(bankName)) {
                return bank;
            }
        }
        return UNKNOWN;
    }
}
