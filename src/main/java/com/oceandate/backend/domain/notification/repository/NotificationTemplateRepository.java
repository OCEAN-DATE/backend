package com.oceandate.backend.domain.notification.repository;

import com.oceandate.backend.domain.notification.entity.NotificationTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, Integer> {
}
