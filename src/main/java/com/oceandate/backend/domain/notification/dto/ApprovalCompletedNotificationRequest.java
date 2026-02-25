package com.oceandate.backend.domain.notification.dto;

import com.oceandate.backend.domain.notification.entity.RelatedEntityType;
import com.oceandate.backend.domain.user.entity.Member;

import java.time.LocalDateTime;

public record ApprovalCompletedNotificationRequest(
        Member member,
        String eventName,
        LocalDateTime eventDateTimeOrNull,
        String paymentUrl,
        RelatedEntityType relatedEntityType,
        Long relatedEntityId,
        String clientIdempotencyKey
) {
}
