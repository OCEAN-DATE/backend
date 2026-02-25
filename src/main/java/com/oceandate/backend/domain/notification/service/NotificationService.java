package com.oceandate.backend.domain.notification.service;

import com.oceandate.backend.domain.notification.dto.ApprovalCompletedNotificationRequest;
import com.oceandate.backend.domain.notification.dto.DayBeforeReminderNotificationRequest;
import com.oceandate.backend.domain.notification.dto.MatchingSuccessNotificationRequest;
import com.oceandate.backend.domain.notification.dto.NotificationInboxItemResponse;
import com.oceandate.backend.domain.notification.dto.PaymentCompletedNotificationRequest;
import com.oceandate.backend.domain.notification.entity.NotificationHistory;
import com.oceandate.backend.domain.notification.entity.NotificationStatus;
import com.oceandate.backend.domain.notification.entity.NotificationType;
import com.oceandate.backend.domain.notification.entity.RelatedEntityType;
import com.oceandate.backend.domain.notification.repository.NotificationHistoryRepository;
import com.oceandate.backend.domain.notification.service.sender.NotificationSender;
import com.oceandate.backend.domain.user.entity.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final NotificationSender emailSender;
    private final NotificationHistoryRepository historyRepository;
    private final NotificationHistoryWriter historyWriter;
    private final NotificationIdempotencyService idempotencyService;
    private final NotificationTemplateService templateService;

    @Transactional
    public void sendApprovalCompleted(ApprovalCompletedNotificationRequest request) {
        NotificationTemplateService.RenderedTemplate rendered = templateService.renderEmail(
                NotificationType.APPROVAL_COMPLETED,
                approvalCompletedVariables(request)
        );

        sendWithHistory(
                request.member(),
                NotificationType.APPROVAL_COMPLETED,
                request.member().getEmail(),
                rendered.title(),
                rendered.bodyHtml(),
                request.relatedEntityType(),
                request.relatedEntityId(),
                request.clientIdempotencyKey()
        );
    }

    @Transactional
    public void sendPaymentCompleted(PaymentCompletedNotificationRequest request) {
        NotificationTemplateService.RenderedTemplate rendered = templateService.renderEmail(
                NotificationType.PAYMENT_COMPLETED,
                paymentCompletedVariables(request)
        );

        sendWithHistory(
                request.member(),
                NotificationType.PAYMENT_COMPLETED,
                request.member().getEmail(),
                rendered.title(),
                rendered.bodyHtml(),
                request.relatedEntityType(),
                request.relatedEntityId(),
                request.clientIdempotencyKey()
        );
    }

    @Transactional
    public void sendDayBeforeReminder(DayBeforeReminderNotificationRequest request) {
        NotificationTemplateService.RenderedTemplate rendered = templateService.renderEmail(
                NotificationType.DAY_BEFORE_REMINDER,
                dayBeforeReminderVariables(request)
        );

        sendWithHistory(
                request.member(),
                NotificationType.DAY_BEFORE_REMINDER,
                request.member().getEmail(),
                rendered.title(),
                rendered.bodyHtml(),
                request.relatedEntityType(),
                request.relatedEntityId(),
                request.clientIdempotencyKey()
        );
    }

    @Transactional
    public void sendMatchingSuccess(MatchingSuccessNotificationRequest request) {
        NotificationTemplateService.RenderedTemplate rendered = templateService.renderEmail(
                NotificationType.MATCHING_SUCCESS,
                matchingSuccessVariables(request)
        );

        sendWithHistory(
                request.member(),
                NotificationType.MATCHING_SUCCESS,
                request.member().getEmail(),
                rendered.title(),
                rendered.bodyHtml(),
                request.relatedEntityType(),
                request.relatedEntityId(),
                request.clientIdempotencyKey()
        );
    }

    @Transactional(readOnly = true)
    public List<NotificationInboxItemResponse> getInbox(Long memberId, int limit) {
        int normalizedLimit = Math.max(1, Math.min(limit, 50));

        return historyRepository
                .findTop50ByMemberIdAndStatusOrderBySentAtDescIdDesc(memberId, NotificationStatus.SENT)
                .stream()
                .limit(normalizedLimit)
                .map(NotificationInboxItemResponse::from)
                .toList();
    }

    private void sendWithHistory(
            Member member,
            NotificationType type,
            String to,
            String title,
            String content,
            RelatedEntityType relatedEntityType,
            Long relatedEntityId,
            String clientIdempotencyKey
    ) {
        String idempotencyKey = buildIdempotencyKey(member.getId(), type, relatedEntityType, relatedEntityId, clientIdempotencyKey);
        NotificationIdempotencyService.AcquireStatus acquireStatus = idempotencyService.acquire(idempotencyKey);

        if (acquireStatus == NotificationIdempotencyService.AcquireStatus.COMPLETED
                || acquireStatus == NotificationIdempotencyService.AcquireStatus.PROCESSING) {
            log.info("Skip duplicate notification send. key={}, status={}", idempotencyKey, acquireStatus);
            return;
        }

        if (isAlreadySent(member, type, relatedEntityType, relatedEntityId)) {
            log.info(
                    "Skip already sent notification. memberId={}, type={}, relatedEntityType={}, relatedEntityId={}",
                    member.getId(), type, relatedEntityType, relatedEntityId
            );
            idempotencyService.complete(idempotencyKey);
            return;
        }

        try {
            emailSender.sendHtml(to, title, content);
            saveHistorySafely(NotificationHistory.sent(
                    member, type, to, title, content, relatedEntityType, relatedEntityId
            ));
            idempotencyService.complete(idempotencyKey);
        } catch (Exception sendException) {
            saveHistorySafely(NotificationHistory.failed(
                    member, type, to, title, content,
                    relatedEntityType, relatedEntityId, sendException.getMessage()
            ));
            idempotencyService.release(idempotencyKey);
            log.warn(
                    "Notification send failed. memberId={}, type={}, relatedEntityType={}, relatedEntityId={}, reason={}",
                    member.getId(), type, relatedEntityType, relatedEntityId, sendException.getMessage()
            );
        }
    }

    private boolean isAlreadySent(
            Member member,
            NotificationType type,
            RelatedEntityType relatedEntityType,
            Long relatedEntityId
    ) {
        return historyRepository.existsByMemberIdAndTypeAndRelatedEntityTypeAndRelatedEntityIdAndStatus(
                member.getId(),
                type,
                relatedEntityType,
                relatedEntityId,
                NotificationStatus.SENT
        );
    }

    private void saveHistorySafely(NotificationHistory history) {
        try {
            historyWriter.save(history);
        } catch (Exception historyException) {
            log.error(
                    "Failed to save notification history. memberId={}, type={}, status={}",
                    history.getMember().getId(), history.getType(), history.getStatus(), historyException
            );
        }
    }

    private String buildIdempotencyKey(
            Long memberId,
            NotificationType type,
            RelatedEntityType relatedEntityType,
            Long relatedEntityId,
            String clientIdempotencyKey
    ) {
        String normalizedClientKey = normalizeClientIdempotencyKey(clientIdempotencyKey);
        if (normalizedClientKey != null) {
            return String.join("|",
                    "CLIENT",
                    String.valueOf(memberId),
                    type.name(),
                    relatedEntityType == null ? "NONE" : relatedEntityType.name(),
                    String.valueOf(relatedEntityId),
                    normalizedClientKey
            );
        }
        return String.join("|",
                String.valueOf(memberId),
                type.name(),
                relatedEntityType == null ? "NONE" : relatedEntityType.name(),
                String.valueOf(relatedEntityId)
        );
    }

    private String normalizeClientIdempotencyKey(String clientIdempotencyKey) {
        if (clientIdempotencyKey == null) {
            return null;
        }
        String trimmed = clientIdempotencyKey.trim();
        if (trimmed.isBlank()) {
            return null;
        }
        // Keep server-generated delimiter semantics stable.
        return trimmed.replace("|", "_");
    }

    private Map<String, Object> approvalCompletedVariables(ApprovalCompletedNotificationRequest request) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("eventName", nullToEmpty(request.eventName()));
        variables.put("eventDateTimeBlock", buildInfoBlock("일정", formatDateTime(request.eventDateTimeOrNull())));
        variables.put("paymentUrlBlock", buildPaymentUrlBlock(request.paymentUrl()));
        return variables;
    }

    private Map<String, Object> paymentCompletedVariables(PaymentCompletedNotificationRequest request) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("orderId", nullToEmpty(request.orderId()));
        variables.put("amount", request.amount() == null ? "" : request.amount());
        variables.put("eventName", nullToEmpty(request.eventName()));
        variables.put("eventDateTime", formatDateTime(request.eventDateTimeOrNull()));
        variables.put("eventDateTimeBlock", buildInfoBlock("일정", formatDateTime(request.eventDateTimeOrNull())));
        return variables;
    }

    private Map<String, Object> dayBeforeReminderVariables(DayBeforeReminderNotificationRequest request) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("eventName", nullToEmpty(request.eventName()));
        variables.put("eventDateTime", formatDateTime(request.eventDateTime()));
        variables.put("place", nullToEmpty(request.place()));
        variables.put("placeBlock", buildInfoBlock("장소", request.place()));
        return variables;
    }

    private Map<String, Object> matchingSuccessVariables(MatchingSuccessNotificationRequest request) {
        Map<String, Object> variables = new LinkedHashMap<>();
        variables.put("eventName", nullToEmpty(request.eventName()));
        variables.put("partnerNickname", nullToEmpty(request.partnerNickname()));
        return variables;
    }

    private String buildInfoBlock(String label, String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return "<p style=\"margin:0 0 8px;\"><strong>" + HtmlUtils.htmlEscape(label) + "</strong>: "
                + HtmlUtils.htmlEscape(value) + "</p>";
    }

    private String buildPaymentUrlBlock(String paymentUrl) {
        if (paymentUrl == null || paymentUrl.isBlank()) {
            return "";
        }
        String escapedUrl = HtmlUtils.htmlEscape(paymentUrl);
        return "<p style=\"margin:0;\"><strong>결제 링크</strong>: <a href=\"" + escapedUrl + "\">" + escapedUrl + "</a></p>";
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
