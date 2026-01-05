package com.oceandate.backend.domain.matching.dto;

import com.fasterxml.jackson.databind.deser.DataFormatReaders;
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
    private String idealType;
    private String hobby;
    private ApplicationStatus status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
    private String orderId;

    private Long userId;
    private String name;
    private String email;

    private MatchedUserInfo matchedUserInfo;

    public static OneToOneResponse from(OneToOne oneToOne) {
        return OneToOneResponse.builder()
                .id(oneToOne.getId())
                .job(oneToOne.getJob())
                .introduction(oneToOne.getIntroduction())
                .idealType(oneToOne.getIdealType())
                .hobby(oneToOne.getHobby())
                .status(oneToOne.getStatus())
                .amount(oneToOne.getAmount())
                .createdAt(oneToOne.getCreatedAt())
                .orderId(oneToOne.getOrderId())
                .userId(oneToOne.getMember().getId())
                .name(oneToOne.getMember().getName())
                .email(oneToOne.getMember().getEmail())
                .build();
    }

    public static OneToOneResponse from(OneToOne oneToOne, MatchedUserInfo matchedUser) {
        return OneToOneResponse.builder()
                .id(oneToOne.getId())
                .job(oneToOne.getJob())
                .introduction(oneToOne.getIntroduction())
                .idealType(oneToOne.getIdealType())
                .hobby(oneToOne.getHobby())
                .status(oneToOne.getStatus())
                .amount(oneToOne.getAmount())
                .createdAt(oneToOne.getCreatedAt())
                .orderId(oneToOne.getOrderId())
                .userId(oneToOne.getMember().getId())
                .name(oneToOne.getMember().getName())
                .email(oneToOne.getMember().getEmail())
                .matchedUserInfo(matchedUser)
                .build();
    }
}
