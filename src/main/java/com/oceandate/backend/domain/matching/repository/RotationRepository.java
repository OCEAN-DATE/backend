package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.VerificationStatus;
import com.oceandate.backend.domain.user.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface RotationRepository extends JpaRepository<Rotation, Long> {
    List<Rotation> findByMember(Member user);

    List<Rotation> findByEvent(RotationEvent event);

    Page<Rotation> findByEventAndVerificationStatus(
            RotationEvent event,
            VerificationStatus status,
            Pageable pageable);

    List<Rotation> findByEventAndVerificationStatus(
            RotationEvent event,
            VerificationStatus status);

    boolean existsByMemberAndEvent(Member user, RotationEvent event);

    List<Rotation> findByEventAndStatus(RotationEvent event, ApplicationStatus status);

    @Query("SELECT a FROM Rotation a " +
            "WHERE a.status = :status " +
            "AND a.documentDeadline BETWEEN :startTime AND :endTime " +
            "AND a.documentSubmittedAt IS NULL")
    List<Rotation> findByStatusAndDocumentDeadlineBetween(
            ApplicationStatus status,
            LocalDateTime startTime,
            LocalDateTime endTime);

    long countByStatus(ApplicationStatus status);

    List<Rotation> findByStatus(ApplicationStatus status);
}
