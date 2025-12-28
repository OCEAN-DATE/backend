package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OneToOneRepository extends JpaRepository<OneToOne, Long> {
    List<OneToOne> findByUserId(Long userId);
    List<OneToOne> findByStatus(String status);
    Optional<OneToOne> findByOrderId(String orderId);
}
