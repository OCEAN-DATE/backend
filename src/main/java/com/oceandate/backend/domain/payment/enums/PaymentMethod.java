package com.oceandate.backend.domain.payment.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    CARD("카드"),
    VIRTUAL_ACCOUNT("가상계좌"),
    EASY_PAY("간편결제"),
    MOBILE_PHONE("휴대폰"),
    TRANSFER("계좌이체"),
    CULTURE_GIFT_CERTIFICATE("문화상품권"),
    BOOK_GIFT_CERTIFICATE("도서문화상품권"),
    GAME_GIFT_CERTIFICATE("게임문화상품권");

    private final String displayName;

    public static PaymentMethod fromTossMethod(String tossMethod) {
        for (PaymentMethod method : values()) {
            if (method.displayName.equals(tossMethod)) {
                return method;
            }
        }
        throw new IllegalArgumentException("지원하지 않는 결제 수단: " + tossMethod);
    }
}
