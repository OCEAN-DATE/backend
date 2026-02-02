package com.oceandate.backend.domain.matching.dto;

import com.oceandate.backend.domain.matching.entity.TravelEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.review.dto.ReviewResponse;
import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelEventResponse {
    private Long eventId;
    private String eventName;
    private String location;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private Integer maleCapacity;
    private Integer femaleCapacity;
    private Integer approvedMaleCount;
    private Integer approvedFemaleCount;
    private String ageRange;
    private Integer amount;
    private String description;
    private EventStatus status;
    private List<TravelScheduleDto> schedules;
    private List<AccommodationDto> accommodations;
    private List<ReviewResponse> reviews;

    public static TravelEventResponse from(TravelEvent event) {
        return TravelEventResponse.builder()
                .eventId(event.getId())
                .eventName(event.getEventName())
                .location(event.getLocation())
                .eventStartDate(event.getEventStartDate())
                .eventEndDate(event.getEventEndDate())
                .maleCapacity(event.getMaleCapacity())
                .femaleCapacity(event.getFemaleCapacity())
                .approvedMaleCount(event.getApprovedMaleCount())
                .approvedFemaleCount(event.getApprovedFemaleCount())
                .ageRange(event.getAgeRange())
                .amount(event.getAmount())
                .description(event.getDescription())
                .status(event.getStatus())
                .schedules(event.getSchedules().stream()
                        .map(TravelScheduleDto::from)
                        .collect(Collectors.toList()))
                .accommodations(event.getAccommodations().stream()
                        .map(AccommodationDto::from)
                        .collect(Collectors.toList()))
                .build();
    }

    public static TravelEventResponse fromWithReviews(TravelEvent event, List<ReviewResponse> reviews) {
        TravelEventResponse response = from(event);
        response.setReviews(reviews);
        return response;
    }
}
