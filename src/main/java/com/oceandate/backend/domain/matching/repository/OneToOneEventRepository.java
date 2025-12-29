package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OneToOneEventRepository extends JpaRepository<OneToOneEvent, Long> {
    List<OneToOneEvent> findByStatus(EventStatus status);
}
