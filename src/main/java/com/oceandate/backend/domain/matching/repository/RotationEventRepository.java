package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RotationEventRepository extends JpaRepository<RotationEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM RotationEvent e WHERE e.id = :id")
    Optional<RotationEvent> findByIdWithLock(Long id);

    List<RotationEvent> findByStatus(EventStatus status);

    List<RotationEvent> findByStatusAndEventDateTimeBefore(EventStatus status, LocalDateTime dateTime);

    List<RotationEvent> findByEventDateTimeBetween(LocalDateTime start, LocalDateTime end);
}
