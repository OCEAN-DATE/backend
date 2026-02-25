package com.oceandate.backend.domain.notification.service.sender;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailSender implements NotificationSender {
    private final JavaMailSender mailSender;

    @Override
    public void send(String email, String title, String content) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setText(content);
        message.setSubject(title);

        mailSender.send(message);
    }

    @Override
    public void sendHtml(String email, String title, String htmlContent) {
        try {
            var message = mailSender.createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(email);
            helper.setSubject(title);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to send html email", e);
        }
    }
}
