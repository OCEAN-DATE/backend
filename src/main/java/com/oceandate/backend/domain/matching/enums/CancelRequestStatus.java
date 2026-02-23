package com.oceandate.backend.domain.matching.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum CancelRequestStatus {
    PENDING("대기중"),
    APPROVED("승인완료"),
    REJECTED("거절됨");

    private final String description;
}
