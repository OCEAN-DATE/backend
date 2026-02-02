package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.TravelEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TravelEventRepository extends JpaRepository<TravelEvent, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM TravelEvent e WHERE e.id = :id")
    Optional<TravelEvent> findByIdWithLock(Long id);

    @Query("SELECT e FROM TravelEvent e LEFT JOIN FETCH e.schedules LEFT JOIN FETCH e.accommodations WHERE e.id = :id")
    Optional<TravelEvent> findByIdWithDetails(Long id);

    List<TravelEvent> findByStatus(EventStatus status);

    List<TravelEvent> findByStatusAndEventStartDateBefore(EventStatus status, LocalDate date);

    List<TravelEvent> findByEventStartDateBetween(LocalDate start, LocalDate end);
}
