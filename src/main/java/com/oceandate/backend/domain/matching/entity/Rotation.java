package com.oceandate.backend.domain.matching.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "rotation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Rotation extends Matching{

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private RotationEvent event;

    @Column
    private LocalDateTime approvedAt;

    @Column
    @Builder.Default
    private Boolean refunded = false;

    @Column
    private LocalDateTime refundedAt;
}
