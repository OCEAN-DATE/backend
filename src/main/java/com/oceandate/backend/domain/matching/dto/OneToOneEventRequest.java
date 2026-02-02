package com.oceandate.backend.domain.matching.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private MultipartFile image;
}
