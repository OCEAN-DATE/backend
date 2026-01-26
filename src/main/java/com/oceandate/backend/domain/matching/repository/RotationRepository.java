package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.user.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RotationRepository extends JpaRepository<Rotation, Long> {
    List<Rotation> findByMember(Member user);

    boolean existsByMemberAndEvent(Member user, RotationEvent event);

    List<Rotation> findByStatus(ApplicationStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Rotation o WHERE o.orderId = :orderId")
    Optional<Rotation> findByOrderIdWithLock(String orderId);

    List<Rotation> findByMemberId(Long memberId);

    @Query("SELECT o FROM Rotation o WHERE o.event.id = :eventId " +
            "AND (o.status = 'APPROVED' OR o.status = 'PAYMENT_PENDING' OR o.status = 'PAYMENT_COMPLETED')")
    List<Rotation> findByEventIdAndApproved(Long eventId);

    @Query("SELECT o FROM Rotation o WHERE o.event.id = :eventId")
    List<Rotation> findByEventId(Long eventId);

    @Query("SELECT o FROM Rotation o WHERE o.event.id = :eventId AND o.status = :status")
    List<Rotation> findByEventIdAndStatus(ApplicationStatus status);
}
