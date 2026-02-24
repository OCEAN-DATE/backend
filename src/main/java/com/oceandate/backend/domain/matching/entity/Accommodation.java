package com.oceandate.backend.domain.matching.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

@Entity
@Table(name = "accommodation")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;  // 숙소명

    @Column(nullable = false)
    private String address;  // 숙소 주소

    @Column(nullable = false)
    private LocalTime checkInTime;  // 체크인 시간

    @Column(nullable = false)
    private LocalTime checkOutTime;  // 체크아웃 시간

    @Column(columnDefinition = "TEXT")
    private String description;  // 숙소 설명

    @Column
    private String imageUrl;  // 숙소 이미지 URL
}
