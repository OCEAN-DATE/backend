package com.oceandate.backend.domain.matching.repository;

import com.oceandate.backend.domain.matching.entity.Travel;
import com.oceandate.backend.domain.matching.entity.TravelEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.user.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TravelRepository extends JpaRepository<Travel, Long> {
    List<Travel> findByMember(Member member);

    Optional<Travel> findByMemberAndEvent(Member member, TravelEvent event);

    List<Travel> findByEvent(TravelEvent event);

    List<Travel> findByStatus(ApplicationStatus status);

    Page<Travel> findByEventAndStatus(
            TravelEvent event,
            ApplicationStatus status,
            Pageable pageable);

    List<Travel> findByEventAndStatus(
            TravelEvent event,
            ApplicationStatus status);

    boolean existsByMemberAndEvent(Member member, TravelEvent event);

    long countByStatus(ApplicationStatus status);

    long countByEventAndStatus(TravelEvent event, ApplicationStatus status);

    @Query("SELECT t FROM Travel t WHERE t.member = :member AND t.event = :event " +
            "AND t.status = :status AND t.createdAt >= :since")
    Optional<Travel> findByMemberAndEventAndStatusAndCreatedAtAfter(
            @Param("member") Member member,
            @Param("event") TravelEvent event,
            @Param("status") ApplicationStatus status,
            @Param("since") LocalDateTime since
    );

    @Query("SELECT t FROM Travel t WHERE t.status = :status AND t.createdAt < :before")
    List<Travel> findByStatusAndCreatedAtBefore(
            @Param("status") ApplicationStatus status,
            @Param("before") LocalDateTime before
    );

    // 관리자용: Member와 Event를 함께 조회
    @Query("SELECT t FROM Travel t JOIN FETCH t.member JOIN FETCH t.event")
    List<Travel> findAllWithMemberAndEvent();

    @Query("SELECT t FROM Travel t JOIN FETCH t.member JOIN FETCH t.event WHERE t.status = :status")
    List<Travel> findByStatusWithMemberAndEvent(@Param("status") ApplicationStatus status);

    @Query("SELECT t FROM Travel t JOIN FETCH t.member JOIN FETCH t.event WHERE t.event = :event")
    List<Travel> findByEventWithMemberAndEvent(@Param("event") TravelEvent event);

    @Query("SELECT t FROM Travel t JOIN FETCH t.member JOIN FETCH t.event WHERE t.event = :event AND t.status = :status")
    List<Travel> findByEventAndStatusWithMemberAndEvent(
            @Param("event") TravelEvent event,
            @Param("status") ApplicationStatus status
    );
    Optional<Travel> findByOrderId(String orderId);

    @Query("""
                SELECT t FROM Travel t
                JOIN FETCH t.member m
                JOIN FETCH t.event e
                WHERE t.status = 'APPROVED'
                  AND e.eventStartDate = :date
            """)
    List<Travel> findApprovedByEventStartDate(@Param("date") LocalDate date);

}
