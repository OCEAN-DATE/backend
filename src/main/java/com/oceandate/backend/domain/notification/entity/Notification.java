package com.oceandate.backend.domain.notification.entity;

import com.oceandate.backend.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@EntityListeners(AuditingEntityListener.class)
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_notification_idem_channel",
                        columnNames = {"idempotency_key", "channel"}
                )
        },
        indexes = {
                @Index(name = "idx_notification_status_scheduled",
                    columnList = "status, scheduled_at"
                )
        }
)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notification_template_id", nullable = false)
    private NotificationTemplate template;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationChannel channel;

    @Column(nullable = false)
    private int retryCount = 0;

    @Column(nullable = false, name = "scheduled_at")
    private LocalDateTime scheduledAt;
    private LocalDateTime sentAt;

    @CreatedDate
    @Column(updatable = false, nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    public void markSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markRetry(LocalDateTime nextScheduledAt) {
        retryCount++;
        this.status = NotificationStatus.RETRYING;
        this.scheduledAt = nextScheduledAt;
    }

    public void markFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public static Notification create(UserEntity user, NotificationTemplate template, NotificationChannel channel, LocalDateTime scheduledAt, String idempotencyKey) {
        Notification notification = new Notification();
        notification.user = user;
        notification.template = template;
        notification.status = NotificationStatus.PENDING;
        notification.channel = channel;
        notification.retryCount = 0;
        notification.scheduledAt = scheduledAt;
        notification.sentAt = null;
        notification.idempotencyKey = idempotencyKey;
        return notification;
    }
}
