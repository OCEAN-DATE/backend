package com.oceandate.backend.domain.matching.dto;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.review.dto.ReviewResponse;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RotationEventResponse {
    private Long eventId;
    private String eventName;
    private LocalDateTime eventDateTime;
    private Integer maleCapacity;
    private Integer femaleCapacity;
    private Integer approvedMaleCount;
    private Integer approvedFemaleCount;
    private String ageRange;
    private String location;
    private Integer amount;
    private String description;

    private String imageUrl;
    private List<ReviewResponse> reviews;

    public static RotationEventResponse from(RotationEvent rotationEvent) {
        return RotationEventResponse.builder()
                .eventId(rotationEvent.getId())
                .eventName(rotationEvent.getEventName())
                .eventDateTime(rotationEvent.getEventDateTime())
                .maleCapacity(rotationEvent.getMaleCapacity())
                .femaleCapacity(rotationEvent.getFemaleCapacity())
                .approvedMaleCount(rotationEvent.getApprovedMaleCount())
                .approvedFemaleCount(rotationEvent.getApprovedFemaleCount())
                .ageRange(rotationEvent.getAgeRange())
                .location(rotationEvent.getLocation())
                .description(rotationEvent.getDescription())
                .build();
    }

    public static RotationEventResponse fromDetail(RotationEvent rotationEvent, List<ReviewResponse> reviews) {
        return RotationEventResponse.builder()
                .eventId(rotationEvent.getId())
                .eventName(rotationEvent.getEventName())
                .eventDateTime(rotationEvent.getEventDateTime())
                .maleCapacity(rotationEvent.getMaleCapacity())
                .femaleCapacity(rotationEvent.getFemaleCapacity())
                .approvedMaleCount(rotationEvent.getApprovedMaleCount())
                .approvedFemaleCount(rotationEvent.getApprovedFemaleCount())
                .ageRange(rotationEvent.getAgeRange())
                .location(rotationEvent.getLocation())
                .description(rotationEvent.getDescription())
                .imageUrl(rotationEvent.getS3Url())
                .reviews(reviews)
                .build();

    }
}

