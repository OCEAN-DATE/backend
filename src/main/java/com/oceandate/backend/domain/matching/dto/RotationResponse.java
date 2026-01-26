package com.oceandate.backend.domain.matching.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.entity.Sex;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RotationResponse {
    private Long id;
    private ApplicationStatus status;
    private String orderId;
    private Integer amount;
    private String orderName;
    private String customerEmail;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private Long userId;
    private String name;
    private Sex sex;
    private String email;

    public static RotationResponse from(Rotation rotation) {
        return RotationResponse.builder()
                .id(rotation.getId())
                .status(rotation.getStatus())
                .amount(rotation.getAmount())
                .createdAt(rotation.getCreatedAt())
                .approvedAt(rotation.getApprovedAt())
                .orderId(rotation.getOrderId())
                .userId(rotation.getMember().getId())
                .name(rotation.getMember().getName())
                .sex(rotation.getMember().getSex())
                .email(rotation.getMember().getEmail())
                .build();
    }
}