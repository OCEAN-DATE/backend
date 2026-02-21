package com.oceandate.backend.domain.matching.dto;

import com.oceandate.backend.domain.user.entity.Sex;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class OneToOneRequest {
    private Long eventId;
    private Sex sex;
    private List<LocalDate> preferredDates;
    private String introduction;
    private String job;
    private String hobby;
    private String idealType;
}
