package com.oceandate.backend.domain.matching.dto;
import lombok.*;

import java.math.BigDecimal;
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
    private BigDecimal amount;
    private String description;
}

