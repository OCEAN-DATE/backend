package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.user.entity.Member;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RotationRepository extends JpaRepository<Rotation, Long> {
    List<Rotation> findByMember(Member user);

    boolean existsByMemberAndEvent(Member user, RotationEvent event);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Rotation o WHERE o.orderId = :orderId")
    Optional<Rotation> findByOrderIdWithLock(String orderId);

    Optional<Rotation> findByOrderId(String orderId);

    List<Rotation> findByMemberId(Long memberId);

    @Query("SELECT o FROM Rotation o WHERE o.event.id = :eventId " +
            "AND (o.status = 'APPROVED' OR o.status = 'PAYMENT_PENDING' OR o.status = 'PAYMENT_COMPLETED')")
    List<Rotation> findByEventIdAndApproved(Long eventId);

    @Query("SELECT r FROM Rotation r JOIN FETCH r.member JOIN FETCH r.event WHERE r.event.id = :eventId")
    List<Rotation> findByEventId(Long eventId);

    @Query("SELECT r FROM Rotation r JOIN FETCH r.member JOIN FETCH r.event WHERE r.event.id = :eventId AND r.status = :status")
    List<Rotation> findByEventIdAndStatus(Long eventId, ApplicationStatus status);

    @Query("SELECT o FROM Rotation o WHERE o.id = :applicationId AND o.event.id = :eventId")
    Optional<Rotation> findByEventIdAndApplicationId(Long eventId, Long applicationId);

    Optional<Rotation> findFirstByMemberIdOrderByCreatedAtDesc(Long memberId);

    @Query("""
        SELECT r FROM Rotation r
        JOIN FETCH r.member m
        JOIN FETCH r.event e
        WHERE r.status = 'APPROVED'
          AND e.eventDateTime >= :start
          AND e.eventDateTime < :end
    """)
    List<Rotation> findApprovedByEventDateTimeBetween(
            @Param("start") java.time.LocalDateTime start,
            @Param("end") java.time.LocalDateTime end
    );

}
