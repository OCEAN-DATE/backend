package com.oceandate.backend.domain.matching.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MatchingCreateRequest {
    private Long eventId;
    private Long maleApplicationId;
    private Long femaleApplicationId;
    private LocalDateTime confirmedDate;
}
