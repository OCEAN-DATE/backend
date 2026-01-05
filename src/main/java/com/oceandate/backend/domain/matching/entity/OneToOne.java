package com.oceandate.backend.domain.matching.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Table(name = "one_to_one")
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class OneToOne extends Matching {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private OneToOneEvent event;

    @ElementCollection
    @CollectionTable(name = "preferred_dates",
            joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "preferred_date")
    private List<LocalDate> preferredDates;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String idealType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String hobby;
}
