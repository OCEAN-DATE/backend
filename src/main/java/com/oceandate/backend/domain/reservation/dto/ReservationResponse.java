package com.oceandate.backend.domain.reservation.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ReservationResponse {
    private Long reservationId;

    private String orderId;
    private String orderName;
    private BigDecimal finalAmount;
}
