package com.oceandate.backend.domain.matching.entity;

import com.oceandate.backend.domain.matching.enums.VerificationStatus;
import com.oceandate.backend.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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

    @Column(name = "employment_certificate_url")
    private String employmentCertificateUrl;

    @Column(name = "identity_certificate_url")
    private String identityCertificateUrl;

    @Column(nullable = false)
    private LocalDateTime documentDeadline;

    @Column
    private LocalDateTime documentSubmittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationStatus verificationStatus;

    @Column
    private String rejectionReason;

    @Column
    private LocalDateTime approvedAt;

    @Column
    private Boolean refunded = false;

    @Column
    private LocalDateTime refundedAt;

    public boolean isDocumentOverdue() {
        return LocalDateTime.now().isAfter(documentDeadline)
                && documentSubmittedAt == null;
    }
}
