package com.oceandate.backend.domain.matching.entity;

import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.user.entity.Sex;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "travel_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private LocalDate eventStartDate;

    @Column(nullable = false)
    private LocalDate eventEndDate;

    @Column(nullable = false)
    private Integer maleCapacity;

    @Column(nullable = false)
    private Integer femaleCapacity;

    @Column(nullable = false)
    @Builder.Default
    private Integer approvedMaleCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer approvedFemaleCount = 0;

    @Column(nullable = false)
    private String ageRange;

    @Column(nullable = false)
    private Integer amount;  // 참가비

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<TravelSchedule> schedules = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Accommodation> accommodations = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Travel> applications = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = EventStatus.OPEN;
        }
        if (approvedMaleCount == null) {
            approvedMaleCount = 0;
        }
        if (approvedFemaleCount == null) {
            approvedFemaleCount = 0;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean canApproveMale() {
        return approvedMaleCount < maleCapacity;
    }

    public boolean canApproveFemale() {
        return approvedFemaleCount < femaleCapacity;
    }

    public boolean isFull() {
        return approvedMaleCount >= maleCapacity && approvedFemaleCount >= femaleCapacity;
    }

    public void incrementApprovedCount(Sex sex) {
        if (Sex.MAN.equals(sex)) {
            if (!canApproveMale()) {
                throw new CustomException(ErrorCode.MALE_CAPACITY_FULL);
            }
            this.approvedMaleCount++;
        } else if (Sex.WOMAN.equals(sex)) {
            if (!canApproveFemale()) {
                throw new CustomException(ErrorCode.FEMALE_CAPACITY_FULL);
            }
            this.approvedFemaleCount++;
        }

        if (isFull()) {
            this.status = EventStatus.CLOSED;
        }
    }

    public void decrementApprovedCount(Sex sex) {
        if (Sex.MAN.equals(sex) && this.approvedMaleCount > 0) {
            this.approvedMaleCount--;
        } else if (Sex.WOMAN.equals(sex) && this.approvedFemaleCount > 0) {
            this.approvedFemaleCount--;
        }

        if (this.status == EventStatus.CLOSED) {
            this.status = EventStatus.OPEN;
        }
    }
}
