package com.oceandate.backend.domain.notification.dto;

import com.oceandate.backend.domain.notification.entity.NotificationHistory;
import com.oceandate.backend.domain.notification.entity.NotificationType;
import com.oceandate.backend.domain.notification.entity.RelatedEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationInboxItemResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String contentHtml;
    private String previewText;
    private RelatedEntityType relatedEntityType;
    private Long relatedEntityId;
    private LocalDateTime sentAt;

    public static NotificationInboxItemResponse from(NotificationHistory history) {
        return NotificationInboxItemResponse.builder()
                .id(history.getId())
                .type(history.getType())
                .title(history.getTitle())
                .contentHtml(history.getContent())
                .previewText(toPreview(history.getContent()))
                .relatedEntityType(history.getRelatedEntityType())
                .relatedEntityId(history.getRelatedEntityId())
                .sentAt(history.getSentAt())
                .build();
    }

    private static String toPreview(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        String noTags = html.replaceAll("(?is)<style.*?>.*?</style>", " ")
                .replaceAll("(?is)<script.*?>.*?</script>", " ")
                .replaceAll("(?is)<[^>]+>", " ");
        return noTags.replaceAll("\\s+", " ").trim();
    }
}
