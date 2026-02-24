package com.oceandate.backend.domain.matching.dto;

import com.oceandate.backend.domain.matching.entity.TravelEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelEventRequest {
    private String eventName;
    private String location;
    private LocalDate eventStartDate;
    private LocalDate eventEndDate;
    private Integer maleCapacity;
    private Integer femaleCapacity;
    private String ageRange;
    private Integer amount;
    private String description;
    private List<Long> accommodationIds;  // 연결할 숙소 ID 리스트

    public TravelEvent toEntity() {
        return TravelEvent.builder()
                .eventName(eventName)
                .location(location)
                .eventStartDate(eventStartDate)
                .eventEndDate(eventEndDate)
                .maleCapacity(maleCapacity)
                .femaleCapacity(femaleCapacity)
                .ageRange(ageRange)
                .amount(amount)
                .description(description)
                .status(EventStatus.OPEN)
                .build();
    }
}
