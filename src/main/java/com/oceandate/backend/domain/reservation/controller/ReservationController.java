package com.oceandate.backend.domain.reservation.controller;

import com.oceandate.backend.domain.reservation.dto.CreateNormalReservationRequest;
import com.oceandate.backend.domain.reservation.dto.ReservationResponse;
import com.oceandate.backend.domain.reservation.service.NormalReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final NormalReservationService normalReservationService;

    @PostMapping("/normal")
    public ResponseEntity<ReservationResponse> createNormalReservation(
            @RequestBody CreateNormalReservationRequest request,
            @RequestHeader("User-Id") Long userId) {

        ReservationResponse response = normalReservationService.createReservation(userId, request);
        return ResponseEntity.ok(response);
    }
}