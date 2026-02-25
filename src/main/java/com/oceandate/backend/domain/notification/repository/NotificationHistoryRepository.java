package com.oceandate.backend.domain.notification.repository;

import com.oceandate.backend.domain.notification.entity.NotificationHistory;
import com.oceandate.backend.domain.notification.entity.NotificationStatus;
import com.oceandate.backend.domain.notification.entity.NotificationType;
import com.oceandate.backend.domain.notification.entity.RelatedEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationHistoryRepository extends JpaRepository<NotificationHistory, Long> {
    boolean existsByMemberIdAndTypeAndRelatedEntityTypeAndRelatedEntityIdAndStatus(
            Long memberId,
            NotificationType type,
            RelatedEntityType relatedEntityType,
            Long relatedEntityId,
            NotificationStatus status
    );

    List<NotificationHistory> findTop50ByMemberIdAndStatusOrderBySentAtDescIdDesc(
            Long memberId,
            NotificationStatus status
    );
}
