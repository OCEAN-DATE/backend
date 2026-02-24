package com.oceandate.backend.domain.notification.service.sender;

public interface NotificationSender {
    void send(String email, String title, String content);

    default void sendHtml(String email, String title, String htmlContent) {
        send(email, title, htmlContent);
    }
}
