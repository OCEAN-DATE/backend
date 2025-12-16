package com.oceandate.backend.domain.notification.repository;

import com.oceandate.backend.domain.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
