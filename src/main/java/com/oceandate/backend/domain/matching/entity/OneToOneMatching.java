package com.oceandate.backend.domain.matching.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "one_to_one_matching")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OneToOneMatching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private OneToOneEvent event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "male_application_id", nullable = false)
    private OneToOne maleApplication;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "female_application_id", nullable = false)
    private OneToOne femaleApplication;

    @Column(nullable = false, updatable = false)
    private LocalDateTime matchedAt;

    @PrePersist
    protected void onCreated() {
        matchedAt = LocalDateTime.now();
    }
}
