package com.oceandate.backend.domain.reservation.repository;

import com.oceandate.backend.domain.reservation.entity.NormalReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NormalReservationRepository extends JpaRepository<NormalReservation, Long> {
    Optional<NormalReservation> findByOrderId(String orderId);
}
