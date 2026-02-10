package com.oceandate.backend.domain.matching.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.review.dto.ReviewResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OneToOneEventResponse {
    private Long eventId;
    private String eventName;
    private String location;
    private Integer amount;
    private String description;
    private EventStatus status;

    private String imageUrl;
    private List<ReviewResponse> reviews;

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

    public static OneToOneEventResponse fromDetail(OneToOneEvent event, List<ReviewResponse> reviews) {
        return OneToOneEventResponse.builder()
                .eventId(event.getId())
                .eventName(event.getEventName())
                .location(event.getLocation())
                .amount(event.getAmount())
                .description(event.getDescription())
                .status(event.getStatus())
                .imageUrl(event.getS3Url())
                .reviews(reviews)
                .build();

    }
}
