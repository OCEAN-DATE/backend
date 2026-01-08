package com.oceandate.backend.domain.matching.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OneToOneEventRequest {
    private String eventName;
    private String location;
    private Integer amount;
    private String description;
}
