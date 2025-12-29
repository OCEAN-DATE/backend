package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OneToOneEventRepository extends JpaRepository<OneToOneEvent, Long> {
}
