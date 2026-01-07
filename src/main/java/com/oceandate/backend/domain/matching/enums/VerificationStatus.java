package com.oceandate.backend.domain.matching.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum VerificationStatus {
    PENDING,
    APPROVED,
    REJECTED
}
