package com.oceandate.backend.domain.matching.entity;

import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private String location;

}
