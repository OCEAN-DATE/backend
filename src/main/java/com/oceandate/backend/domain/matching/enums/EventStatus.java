package com.oceandate.backend.domain.matching.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {
    OPEN,
    CLOSED,
    IN_PROGRESS,
    COMPLETED;
}
