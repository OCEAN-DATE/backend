package com.oceandate.backend.domain.reservation.dto;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class CreateNormalReservationRequest {
    private Long roomId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuest;
    private Long couponId;
}
