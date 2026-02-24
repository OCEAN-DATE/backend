package com.oceandate.backend.domain.notification.entity;

import com.oceandate.backend.domain.user.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_history",
        indexes = {
                @Index(name = "idx_notification_member", columnList = "member_id"),
                @Index(name = "idx_notification_type", columnList = "type"),
                @Index(name = "idx_notification_sentAt", columnList = "sent_at")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class NotificationHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Column(nullable = false)
    private String targetEmail;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "related_entity_type", length = 50)
    private RelatedEntityType relatedEntityType;

    @Column(name = "related_entity_id")
    private Long relatedEntityId;

    @Column(name = "sent_at", nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(name = "fail_reason", columnDefinition = "TEXT")
    private String failReason;

    @PrePersist
    protected void onCreate() {
        sentAt = LocalDateTime.now();
    }

    @Builder
    public NotificationHistory(Member member, NotificationType type, NotificationChannel channel, NotificationStatus status,
                               String targetEmail, String title, String content, String failReason, RelatedEntityType relatedEntityType, Long relatedEntityId) {
        this.member = member;
        this.type = type;
        this.channel = channel;
        this.status = status;
        this.relatedEntityType = relatedEntityType;
        this.targetEmail = targetEmail;
        this.title = title;
        this.content = content;
        this.failReason = failReason;
        this.relatedEntityId = relatedEntityId;
    }

    public static NotificationHistory sent(Member member, NotificationType type, String email, String title, String content,
                                           RelatedEntityType relatedEntityType, Long relatedEntityId) {
        return NotificationHistory.builder()
                .member(member)
                .type(type)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.SENT)
                .targetEmail(email)
                .title(title)
                .content(content)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .sentAt(LocalDateTime.now())
                .build();
    }

    public static NotificationHistory failed(Member member, NotificationType type, String email, String title, String content,
                                             RelatedEntityType relatedEntityType, Long relatedEntityId, String failReason) {
        return NotificationHistory.builder()
                .member(member)
                .type(type)
                .channel(NotificationChannel.EMAIL)
                .status(NotificationStatus.FAILED)
                .targetEmail(email)
                .title(title)
                .content(content)
                .relatedEntityType(relatedEntityType)
                .relatedEntityId(relatedEntityId)
                .sentAt(LocalDateTime.now())
                .failReason(failReason)
                .build();
    }
}
