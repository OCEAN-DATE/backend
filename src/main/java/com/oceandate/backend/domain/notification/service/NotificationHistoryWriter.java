package com.oceandate.backend.domain.notification.service;

import com.oceandate.backend.domain.notification.entity.NotificationHistory;
import com.oceandate.backend.domain.notification.repository.NotificationHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class NotificationHistoryWriter {

    private final NotificationHistoryRepository historyRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void save(NotificationHistory history) {
        historyRepository.save(history);
    }
}
