package com.oceandate.backend.domain.matching.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.user.entity.Sex;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OneToOneResponse {
    private Long id;

    private Long userId;
    private String name;
    private Sex sex;
    private String email;

    private ApplicationStatus status;
    private Integer amount;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private String orderId;

    private UserInfo applicantInfo;

    private UserInfo matchedUserInfo;

    // 목록 조회용
    public static OneToOneResponse from(OneToOne oneToOne) {
        return OneToOneResponse.builder()
                .id(oneToOne.getId())
                .status(oneToOne.getStatus())
                .amount(oneToOne.getAmount())
                .createdAt(oneToOne.getCreatedAt())
                .approvedAt(oneToOne.getApprovedAt())
                .orderId(oneToOne.getOrderId())
                .userId(oneToOne.getMember().getId())
                .name(oneToOne.getMember().getName())
                .sex(oneToOne.getMember().getSex())
                .email(oneToOne.getMember().getEmail())
                .build();
    }

    // 상세 조회용 (신청자 정보 포함)
    public static OneToOneResponse fromDetail(OneToOne oneToOne, UserInfo applicantInfo) {
        return OneToOneResponse.builder()
                .id(oneToOne.getId())
                .status(oneToOne.getStatus())
                .amount(oneToOne.getAmount())
                .createdAt(oneToOne.getCreatedAt())
                .approvedAt(oneToOne.getApprovedAt())
                .orderId(oneToOne.getOrderId())
                .userId(oneToOne.getMember().getId())
                .name(oneToOne.getMember().getName())
                .sex(oneToOne.getMember().getSex())
                .email(oneToOne.getMember().getEmail())
                .applicantInfo(applicantInfo)
                .build();
    }

    // 매칭 완료 후 조회용
    public static OneToOneResponse fromMatched(OneToOne oneToOne, UserInfo applicantInfo, UserInfo matchedUser) {
        return OneToOneResponse.builder()
                .id(oneToOne.getId())
                .status(oneToOne.getStatus())
                .amount(oneToOne.getAmount())
                .createdAt(oneToOne.getCreatedAt())
                .approvedAt(oneToOne.getApprovedAt())
                .orderId(oneToOne.getOrderId())
                .userId(oneToOne.getMember().getId())
                .name(oneToOne.getMember().getName())
                .sex(oneToOne.getMember().getSex())
                .email(oneToOne.getMember().getEmail())
                .applicantInfo(applicantInfo)
                .matchedUserInfo(matchedUser)
                .build();
    }
}