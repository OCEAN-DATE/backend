package com.oceandate.backend.domain.matching.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelRequest {
    private Long eventId;
    private String job;
    private String introduction;
}
