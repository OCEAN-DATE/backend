package com.oceandate.backend.domain.notification.service;

import com.oceandate.backend.domain.notification.entity.Notification;
import com.oceandate.backend.domain.notification.entity.NotificationChannel;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class EmailNotificationSender implements NotificationSender {
    private final JavaMailSender mailSender;
    private final SimpleEmailTempalteRenderer renderer;

    private String from;

    @Override
    public NotificationChannel supports() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(Notification notification) throws Exception {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(notification.getUser().getEmail());

        Map<String, Object> vars = Map.ofEntries(
                Map.entry("serviceName", "OceanDate"),
                Map.entry("userName", "김규민"),
                Map.entry("reservationNo", "R-20251215-001"),
                Map.entry("pensionName", "바다펜션"),
                Map.entry("roomName", "오션뷰 101호"),
                Map.entry("checkInDate", "2025-12-20"),
                Map.entry("checkOutDate", "2025-12-21"),
                Map.entry("nights", 1),
                Map.entry("guests", 2),
                Map.entry("paidAmount", "120,000"),
                Map.entry("paymentMethod", "신용카드"),
                Map.entry("paidAt", "2025-12-15 12:34"),
                Map.entry("reservationDetailUrl", "https://oceandate.co.kr/reservations/123"),
                Map.entry("supportEmail", "support@oceandate.co.kr"),
                Map.entry("year", "2025")
        );

        String renderedTitle = renderer.render(notification.getTemplate().getTitle(), vars);
        helper.setSubject(renderedTitle);
        String renderedBody = renderer.render(notification.getTemplate().getBodyTemplate(), vars);
        helper.setText(renderedBody, true);

        mailSender.send(message);
    }
}