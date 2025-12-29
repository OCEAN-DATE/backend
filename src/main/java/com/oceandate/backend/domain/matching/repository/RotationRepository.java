package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.VerificationStatus;
import com.oceandate.backend.domain.user.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RotationRepository extends JpaRepository<Rotation, Long> {
    List<Rotation> findByUser(UserEntity user);

    List<Rotation> findByEvent(RotationEvent event);

    Page<Rotation> findByEventAndVerificationStatus(
            RotationEvent event,
            VerificationStatus status,
            Pageable pageable);

    List<Rotation> findByEventAndVerificationStatus(
            RotationEvent event,
            VerificationStatus status);

    boolean existsByUserAndEvent(UserEntity user, RotationEvent event);

    @Query("SELECT a FROM Rotation a WHERE a.event.id = :eventId AND a.user.email = :email")
    Optional<Rotation> findByEventIdAndUserEmail(
             Long eventId,
             String email);

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
}
