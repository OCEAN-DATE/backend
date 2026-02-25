package com.oceandate.backend.domain.notification.dto;

import com.oceandate.backend.domain.notification.entity.RelatedEntityType;
import com.oceandate.backend.domain.user.entity.Member;

import java.time.LocalDateTime;

public record DayBeforeReminderNotificationRequest(
        Member member,
        String eventName,
        LocalDateTime eventDateTime,
        String place,
        RelatedEntityType relatedEntityType,
        Long relatedEntityId,
        String clientIdempotencyKey
) {
}
