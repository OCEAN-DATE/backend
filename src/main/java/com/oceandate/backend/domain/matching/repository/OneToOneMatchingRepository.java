package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.OneToOneMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OneToOneMatchingRepository extends JpaRepository<OneToOneMatching, Long> {


    @Query("SELECT m FROM OneToOneMatching m " +
            "JOIN FETCH m.event " +
            "JOIN FETCH m.maleApplication ma " +
            "JOIN FETCH ma.member " +
            "JOIN FETCH m.femaleApplication fa " +
            "JOIN FETCH fa.member " +
            "WHERE m.event.id = :eventId " +
            "ORDER BY m.matchedAt DESC")
    List<OneToOneMatching> findAllByEventId(Long eventId);

    boolean existsByMaleApplicationIdOrFemaleApplicationId(Long maleId, Long femaleId);

    @Query("SELECT m FROM OneToOneMatching m " +
            "JOIN FETCH m.event " +
            "JOIN FETCH m.maleApplication ma " +
            "JOIN FETCH ma.member " +
            "JOIN FETCH m.femaleApplication fa " +
            "JOIN FETCH fa.member " +
            "WHERE m.maleApplication.id = :applicationId " +
            "   OR m.femaleApplication.id = :applicationId ")
    Optional<OneToOneMatching> findByApplicationId(Long applicationId);
}
