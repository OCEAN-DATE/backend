package com.oceandate.backend.domain.payment.entity;

import com.oceandate.backend.domain.payment.enums.PaymentMethod;
import com.oceandate.backend.domain.payment.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Data
@Builder
public class Payment{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "normal_reservation_id")
    private NormalReservation normalReservation;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "date_reservation_id")
    private DateReservation dateReservation;

    @Column(nullable = false, unique = true)
    private String orderId; //토스 내부에서 관리하는 별도의 orderId

    @Column(nullable = false, unique = true)
    private String paymentKey; //토스 페이먼트 키

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod method;

    @Column(name = "requested_at")
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "cancel_reason")
    private String cancelReason;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;
}
