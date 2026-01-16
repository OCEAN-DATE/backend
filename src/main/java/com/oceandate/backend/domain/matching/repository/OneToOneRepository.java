package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OneToOneRepository extends JpaRepository<OneToOne, Long> {
    List<OneToOne> findByMemberId(Long userId);
    List<OneToOne> findByStatus(ApplicationStatus status);
    Optional<OneToOne> findByOrderId(String orderId);
    Boolean existsByMemberIdAndEventId(Long memberId, Long eventId);
    Optional<OneToOne> findByIdAndEventId(Long applicationId, Long id);
    Optional<OneToOne> findByPaymentKey(String paymentKey);
}
