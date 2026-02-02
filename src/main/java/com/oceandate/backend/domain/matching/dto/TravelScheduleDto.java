package com.oceandate.backend.domain.matching.dto;

import com.oceandate.backend.domain.matching.entity.TravelSchedule;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelScheduleDto {
    private Long id;
    private Integer day;
    private LocalTime time;
    private String activity;
    private String location;

    public static TravelScheduleDto from(TravelSchedule schedule) {
        return TravelScheduleDto.builder()
                .id(schedule.getId())
                .day(schedule.getDay())
                .time(schedule.getTime())
                .activity(schedule.getActivity())
                .location(schedule.getLocation())
                .build();
    }
}
