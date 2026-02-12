package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.TravelSchedule;
import com.oceandate.backend.domain.matching.entity.TravelEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TravelScheduleRepository extends JpaRepository<TravelSchedule, Long> {
    List<TravelSchedule> findByEventOrderByDayAscTimeAsc(TravelEvent event);

    void deleteByEvent(TravelEvent event);
}
