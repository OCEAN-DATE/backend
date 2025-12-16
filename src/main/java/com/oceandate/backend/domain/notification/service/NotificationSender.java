package com.oceandate.backend.domain.notification.service;

import com.oceandate.backend.domain.notification.entity.Notification;
import com.oceandate.backend.domain.notification.entity.NotificationChannel;

public interface NotificationSender {
    NotificationChannel supports();
    void send(Notification notification) throws Exception;
}
