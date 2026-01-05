package com.oceandate.backend.domain.matching.dto;

import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OneToOneEventResponse {
    private Long eventId;
    private String eventName;
    private String location;
    private BigDecimal amount;
    private String description;
    private EventStatus status;

    public static OneToOneEventResponse from(OneToOneEvent event){
        return OneToOneEventResponse.builder()
                .eventId(event.getId())
                .eventName(event.getEventName())
                .location(event.getLocation())
                .amount(event.getAmount())
                .description(event.getDescription())
                .status(event.getStatus())
                .build();

    }
}
