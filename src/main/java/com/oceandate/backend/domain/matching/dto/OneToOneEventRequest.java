package com.oceandate.backend.domain.matching.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OneToOneEventRequest {
    private String eventName;
    private String location;
    private BigDecimal amount;
    private String description;
}
