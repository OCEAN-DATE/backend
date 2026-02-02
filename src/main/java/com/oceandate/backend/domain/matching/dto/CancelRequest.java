package com.oceandate.backend.domain.matching.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelRequest {
    private String cancelReason;
}
