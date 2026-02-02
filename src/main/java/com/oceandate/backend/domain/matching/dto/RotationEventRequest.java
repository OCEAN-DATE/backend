package com.oceandate.backend.domain.matching.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotationEventRequest {
    private String eventName;
    private LocalDateTime eventDateTime;
    private Integer maleCapacity;
    private Integer femaleCapacity;
    private String ageRange;
    private String location;
    private Integer amount;
    private String description;
    private MultipartFile image;
}
