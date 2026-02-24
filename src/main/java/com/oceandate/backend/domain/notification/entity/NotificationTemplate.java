package com.oceandate.backend.domain.notification.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "notification_template",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notification_template_type_channel", columnNames = {"type", "channel"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NotificationChannel channel;

    @Column(nullable = false, length = 255)
    private String titleTemplate;

    @Lob
    @Column(nullable = false)
    private String bodyTemplate;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public static NotificationTemplate emailDefault(NotificationType type, String titleTemplate, String bodyTemplate) {
        NotificationTemplate template = new NotificationTemplate();
        template.type = type;
        template.channel = NotificationChannel.EMAIL;
        template.titleTemplate = titleTemplate;
        template.bodyTemplate = bodyTemplate;
        template.active = true;
        return template;
    }
}
