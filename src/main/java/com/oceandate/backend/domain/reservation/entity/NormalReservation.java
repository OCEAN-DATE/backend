package com.oceandate.backend.domain.reservation.entity;

import com.oceandate.backend.domain.payment.entity.Payment;
import com.oceandate.backend.domain.reservation.enums.NormalReservationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class NormalReservation extends Reservation {

    @Column(name = "check_in_date", nullable = false)
    private LocalDate checkInDate;

    @Column(name = "check_out_date", nullable = false)
    private LocalDate checkOutDate;

    @Column(name = "number_of_guest")
    private Integer numberOfGuest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NormalReservationStatus status;

    @OneToOne(mappedBy = "normalReservation", cascade = CascadeType.ALL)
    private Payment payment;
}
