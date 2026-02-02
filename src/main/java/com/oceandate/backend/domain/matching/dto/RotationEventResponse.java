package com.oceandate.backend.domain.matching.dto;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
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

    public static RotationEventResponse from(RotationEvent rotationEvent) {
        return RotationEventResponse.builder()
                .eventId(rotationEvent.getId())
                .eventName(rotationEvent.getEventName())
                .eventDateTime(rotationEvent.getEventDateTime())
                .maleCapacity(rotationEvent.getMaleCapacity())
                .femaleCapacity(rotationEvent.getFemaleCapacity())
                .ageRange(rotationEvent.getAgeRange())
                .location(rotationEvent.getLocation())
                .description(rotationEvent.getDescription())
                .build();
    }
}

