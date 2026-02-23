package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.CancelRequest;
import com.oceandate.backend.domain.matching.enums.CancelRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CancelRequestRepository extends JpaRepository<CancelRequest, Long> {
    Optional<CancelRequest> findByApplicationIdAndStatus(Long applicationId, CancelRequestStatus status);

    List<CancelRequest> findByStatusOrderByCreatedAtAsc(CancelRequestStatus status);

    @Query("SELECT cr FROM CancelRequest cr WHERE cr.application.id = :applicationId ORDER BY cr.createdAt DESC")
    List<CancelRequest> findAllByApplicationId(Long applicationId);
}
