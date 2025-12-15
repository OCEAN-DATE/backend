package com.oceandate.backend.domain.reservation.entity;

import com.oceandate.backend.domain.payment.entity.Payment;
import com.oceandate.backend.domain.reservation.enums.DateReservationStatus;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
public class DateReservation {

    @Column(nullable = false)
    private Integer targetYear;

    @Column(nullable = false)
    private Integer targetMonth;

    @Column(name = "confirmed_check_in_date")
    private LocalDate confirmedCheckInDate;

    @Column(name = "confirmed_check_out_date")
    private LocalDate confirmedCheckOutDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DateReservationStatus status;

    @OneToOne(mappedBy = "dateReservation", cascade = CascadeType.ALL)
    private Payment payment;
}
