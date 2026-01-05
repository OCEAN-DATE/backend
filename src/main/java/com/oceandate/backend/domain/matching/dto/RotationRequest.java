package com.oceandate.backend.domain.matching.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotationRequest {

    private Long eventId;
    private String job;
    private String introduction;
}
