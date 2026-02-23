package com.oceandate.backend.domain.matching.dto;

import com.oceandate.backend.domain.user.entity.Sex;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelRequest {
    private Long eventId;
    private Sex sex;
    private String job;
    private String introduction;
}
