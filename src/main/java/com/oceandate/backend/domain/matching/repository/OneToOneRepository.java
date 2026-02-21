package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OneToOneRepository extends JpaRepository<OneToOne, Long> {
    List<OneToOne> findByMemberId(Long userId);
    List<OneToOne> findByStatus(ApplicationStatus status);
    Optional<OneToOne> findByOrderId(String orderId);
    Boolean existsByMemberIdAndEventId(Long memberId, Long eventId);
    Optional<OneToOne> findByIdAndEventId(Long applicationId, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM OneToOne o WHERE o.orderId = :orderId")
    Optional<OneToOne> findByOrderIdWithLock(String orderId);

    @Query("SELECT o FROM OneToOne o WHERE o.event.id = :eventId AND o.status = :applicationStatus")
    List<OneToOne> findByEventIdAndStatus(Long eventId, ApplicationStatus applicationStatus);

    Optional<OneToOne> findFirstByMemberIdOrderByCreatedAtDesc(Long memberId);
}
