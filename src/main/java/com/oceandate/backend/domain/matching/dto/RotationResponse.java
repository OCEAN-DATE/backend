package com.oceandate.backend.domain.matching.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotationResponse {
    private Long applicationId;
    private String orderId;
    private BigDecimal amount;
    private String orderName;
    private String customerEmail;
}