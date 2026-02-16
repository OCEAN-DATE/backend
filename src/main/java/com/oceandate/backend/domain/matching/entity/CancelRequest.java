package com.oceandate.backend.domain.matching.entity;

import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.CancelRequestStatus;
import com.oceandate.backend.domain.user.entity.Member;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "cancel_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CancelRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false)
    private OneToOne application;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    private Member requester;

    @Column(nullable = false)
    private String cancelReason;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus previousStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CancelRequestStatus status;

    private String adminComment;

    private Long refundAmount;

    @CreatedDate
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    // 취소 요청 생성
    public static CancelRequest create(OneToOne application, Member requester, String cancelReason) {
        CancelRequest cancelRequest = new CancelRequest();
        cancelRequest.application = application;
        cancelRequest.requester = requester;
        cancelRequest.cancelReason = cancelReason;
        cancelRequest.previousStatus = application.getStatus();
        cancelRequest.status = CancelRequestStatus.PENDING;
        return cancelRequest;
    }

    // 관리자 승인
    public void approve(Long refundAmount) {
        this.status = CancelRequestStatus.APPROVED;
        this.refundAmount = refundAmount;
        this.processedAt = LocalDateTime.now();
    }

    // 관리자 거절
    public void reject(String adminComment) {
        this.status = CancelRequestStatus.REJECTED;
        this.adminComment = adminComment;
        this.processedAt = LocalDateTime.now();
    }
}