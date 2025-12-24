package com.oceandate.backend.domain.matching.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotationResponse {
    private Long applicationId;
    private String orderId;
    private Long amount;
    private String orderName;
    private String customerEmail;
}