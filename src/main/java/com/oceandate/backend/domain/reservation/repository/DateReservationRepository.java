package com.oceandate.backend.domain.reservation.repository;

import com.oceandate.backend.domain.reservation.entity.DateReservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DateReservationRepository extends JpaRepository<DateReservation, Long> {

    Optional<DateReservation> findByOrderId(String orderId);
}
