package com.oceandate.backend.domain.notification.dto;

import com.oceandate.backend.domain.notification.entity.RelatedEntityType;
import com.oceandate.backend.domain.user.entity.Member;

public record MatchingSuccessNotificationRequest(
        Member member,
        String eventName,
        String partnerNickname,
        RelatedEntityType relatedEntityType,
        Long relatedEntityId,
        String clientIdempotencyKey
) {
}
