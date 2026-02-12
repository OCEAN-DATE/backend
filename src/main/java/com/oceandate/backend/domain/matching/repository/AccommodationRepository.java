package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.Accommodation;
import com.oceandate.backend.domain.matching.entity.TravelEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccommodationRepository extends JpaRepository<Accommodation, Long> {
    List<Accommodation> findByEvent(TravelEvent event);

    void deleteByEvent(TravelEvent event);
}
