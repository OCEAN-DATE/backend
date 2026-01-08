package com.oceandate.backend.domain.matching.dto;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotationEventResponse {
    private Long eventId;
    private String eventName;
    private LocalDateTime eventDateTime;
    private Integer maleCapacity;
    private Integer femaleCapacity;
    private String ageRange;
    private String location;
    private Integer amount;
    private String description;
}

