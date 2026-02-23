package com.oceandate.backend.domain.matching.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oceandate.backend.domain.matching.entity.Travel;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.user.entity.Sex;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TravelApplicationResponse {
    // 신청 정보
    private Long applicationId;
    private ApplicationStatus status;
    private String orderId;
    private Integer amount;
    private LocalDateTime createdAt;
    private LocalDateTime approvedAt;
    private LocalDateTime paidAt;
    private LocalDateTime cancelledAt;
    private String cancelReason;

    // 이벤트 정보
    private Long eventId;
    private String eventName;

    // 신청자 정보
    private Long userId;
    private String userName;
    private String email;
    private String phoneNumber;
    private Sex sex;

    // 신청서 내용
    private String job;
    private String introduction;

    public static TravelApplicationResponse from(Travel travel) {
        return TravelApplicationResponse.builder()
                .applicationId(travel.getId())
                .status(travel.getStatus())
                .orderId(travel.getOrderId())
                .amount(travel.getAmount())
                .createdAt(travel.getCreatedAt())
                .approvedAt(travel.getApprovedAt())
                .paidAt(travel.getPaidAt())
                .cancelledAt(travel.getCancelledAt())
                .cancelReason(travel.getCancelReason())
                .eventId(travel.getEvent().getId())
                .eventName(travel.getEvent().getEventName())
                .userId(travel.getMember().getId())
                .userName(travel.getMember().getName())
                .email(travel.getMember().getEmail())
                .phoneNumber(travel.getMember().getPhoneNumber())
                .sex(travel.getMember().getSex())
                .job(travel.getJob())
                .introduction(travel.getIntroduction())
                .build();
    }
}
