package com.oceandate.backend.domain.notification.repository;

import com.oceandate.backend.domain.notification.entity.NotificationChannel;
import com.oceandate.backend.domain.notification.entity.NotificationTemplate;
import com.oceandate.backend.domain.notification.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Long> {
    Optional<NotificationTemplate> findByTypeAndChannelAndActiveTrue(NotificationType type, NotificationChannel channel);
}
