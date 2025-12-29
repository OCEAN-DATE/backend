package com.oceandate.backend.domain.matching.entity;

import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.user.enums.Gender;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rotation_event")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RotationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventName;

    @Column(nullable = false)
    private LocalDateTime eventDateTime;

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
    private String location;

    @Column(nullable = false)
    private BigDecimal amount;  // 참가비

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

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

    public void incrementApprovedCount(Gender gender) {
        if (Gender.MALE.equals(gender)) {
            if (!canApproveMale()) {
                throw new CustomException(ErrorCode.MALE_CAPACITY_FULL);
            }
            this.approvedMaleCount++;
        } else if (Gender.FEMALE.equals(gender)) {
            if (!canApproveFemale()) {
                throw new CustomException(ErrorCode.FEMALE_CAPACITY_FULL);
            }
            this.approvedFemaleCount++;
        }

        if (isFull()) {
            this.status = EventStatus.CLOSED;
        }
    }

    public void decrementApprovedCount(String gender) {
        if ("남성".equals(gender) && this.approvedMaleCount > 0) {
            this.approvedMaleCount--;
        } else if ("여성".equals(gender) && this.approvedFemaleCount > 0) {
            this.approvedFemaleCount--;
        }

        if (this.status == EventStatus.CLOSED) {
            this.status = EventStatus.OPEN;
        }
    }
}
