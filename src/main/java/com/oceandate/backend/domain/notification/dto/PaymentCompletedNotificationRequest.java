package com.oceandate.backend.domain.notification.dto;

import com.oceandate.backend.domain.notification.entity.RelatedEntityType;
import com.oceandate.backend.domain.user.entity.Member;

import java.time.LocalDateTime;

public record PaymentCompletedNotificationRequest(
        Member member,
        String orderId,
        Integer amount,
        String eventName,
        LocalDateTime eventDateTimeOrNull,
        RelatedEntityType relatedEntityType,
        Long relatedEntityId,
        String clientIdempotencyKey
) {
}
