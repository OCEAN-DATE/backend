package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.OneToOneMatching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OneToOneMatchingRepository extends JpaRepository<OneToOneMatching, Long> {

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

    @Query("SELECT m FROM OneToOneMatching m JOIN FETCH m.maleApplication JOIN FETCH m.femaleApplication JOIN FETCH m.maleApplication.member JOIN FETCH m.femaleApplication.member WHERE m.maleApplication IN :applications OR m.femaleApplication IN :applications")
    List<OneToOneMatching> findByApplications(List<OneToOne> applications);

}
