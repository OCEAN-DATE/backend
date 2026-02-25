package com.oceandate.backend.domain.notification.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NotificationIdempotencyService {

    private static final long TTL_MINUTES = 60;

    private final Map<String, Entry> store = new ConcurrentHashMap<>();

    public AcquireStatus acquire(String idempotencyKey) {
        cleanupExpired();

        final boolean[] created = {false};
        Entry next = store.compute(idempotencyKey, (key, existing) -> {
            if (existing == null || existing.isExpired()) {
                created[0] = true;
                return Entry.inProgress();
            }
            return existing;
        });

        if (created[0]) {
            return AcquireStatus.ACQUIRED;
        }
        if (next.status == Status.COMPLETED) {
            return AcquireStatus.COMPLETED;
        }
        return AcquireStatus.PROCESSING;
    }

    public void complete(String idempotencyKey) {
        store.put(idempotencyKey, Entry.completed());
    }

    public void release(String idempotencyKey) {
        store.computeIfPresent(idempotencyKey, (k, v) -> v.status == Status.IN_PROGRESS ? null : v);
    }

    private void cleanupExpired() {
        LocalDateTime now = LocalDateTime.now();
        store.entrySet().removeIf(e -> e.getValue().expiresAt.isBefore(now));
    }

    public enum AcquireStatus {
        ACQUIRED,
        PROCESSING,
        COMPLETED
    }

    private enum Status {
        IN_PROGRESS,
        COMPLETED
    }

    private static final class Entry {
        private final Status status;
        private final LocalDateTime expiresAt;

        private Entry(Status status, LocalDateTime expiresAt) {
            this.status = status;
            this.expiresAt = expiresAt;
        }

        private static Entry inProgress() {
            return new Entry(
                    Status.IN_PROGRESS,
                    LocalDateTime.now().plusMinutes(TTL_MINUTES)
            );
        }

        private static Entry completed() {
            return new Entry(
                    Status.COMPLETED,
                    LocalDateTime.now().plusMinutes(TTL_MINUTES)
            );
        }

        private boolean isExpired() {
            return expiresAt.isBefore(LocalDateTime.now());
        }
    }
}
