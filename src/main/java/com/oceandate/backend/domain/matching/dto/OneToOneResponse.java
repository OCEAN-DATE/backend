package com.oceandate.backend.domain.matching.dto;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class OneToOneResponse {
    private Long id;
    private String job;
    private String introduction;
    private String location;
    private ApplicationStatus status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String orderId;

    private Long userId;
    private String username;
    private String email;
    private String nickname;

    public static OneToOneResponse from(OneToOne oneToOne) {
        return OneToOneResponse.builder()
                .id(oneToOne.getId())
                .job(oneToOne.getJob())
                .introduction(oneToOne.getIntroduction())
                .location(oneToOne.getLocation())
                .status(oneToOne.getStatus())
                .amount(oneToOne.getAmount())
                .createdAt(oneToOne.getCreatedAt())
                .orderId(oneToOne.getOrderId())
                .userId(oneToOne.getUser().getId())
                .username(oneToOne.getUser().getUsername())
                .email(oneToOne.getUser().getEmail())
                .nickname(oneToOne.getUser().getNickname())
                .build();
    }
}
