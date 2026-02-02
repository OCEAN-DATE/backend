package com.oceandate.backend.domain.matching.dto;

import com.oceandate.backend.domain.matching.entity.Accommodation;
import lombok.*;

import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccommodationDto {
    private Long id;
    private String name;
    private String address;
    private LocalTime checkInTime;
    private LocalTime checkOutTime;
    private String description;
    private String imageUrl;

    public static AccommodationDto from(Accommodation accommodation) {
        return AccommodationDto.builder()
                .id(accommodation.getId())
                .name(accommodation.getName())
                .address(accommodation.getAddress())
                .checkInTime(accommodation.getCheckInTime())
                .checkOutTime(accommodation.getCheckOutTime())
                .description(accommodation.getDescription())
                .imageUrl(accommodation.getImageUrl())
                .build();
    }
}
