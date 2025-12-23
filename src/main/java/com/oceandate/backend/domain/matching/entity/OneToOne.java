package com.oceandate.backend.domain.matching.entity;

import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@Table(name = "one_to_one")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OneToOne {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @ElementCollection
    @CollectionTable(name = "preferred_dates",
                     joinColumns = @JoinColumn(name = "application_id"))
    @Column(name = "preferred_date")
    private List<LocalDate> preferredDates;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String introduction;

    private String location;

    @Column(nullable = false)
    private ApplicationStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = ApplicationStatus.APPLICATION_SUBMITTED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
