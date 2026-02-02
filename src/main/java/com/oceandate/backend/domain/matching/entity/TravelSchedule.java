package com.oceandate.backend.domain.matching.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "travel_schedule")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private TravelEvent event;

    @Column(nullable = false)
    private Integer day;  // 일차 (1일차, 2일차 등)

    @Column(nullable = false)
    private LocalTime time;  // 일정 시간

    @Column(nullable = false, columnDefinition = "TEXT")
    private String activity;  // 활동 내용

    @Column(nullable = false)
    private String location;  // 활동 장소
}
