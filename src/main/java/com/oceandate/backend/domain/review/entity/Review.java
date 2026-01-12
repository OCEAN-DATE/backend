package com.oceandate.backend.domain.review.entity;

import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.user.entity.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "review",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_writer_matching",
                        columnNames = {"writer_id", "matching_type", "matching_id"}
                )
        }
)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member writer;

    @Enumerated(EnumType.STRING)
    @Column(name = "matching_type", nullable = false, length = 20)
    private MatchingType matchingType;

    @Column(name = "matching_id", nullable = false)
    private Long matchingId;

    @Column(nullable = false)
    private Integer rating;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public void validateRating() {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("평점은 1에서 5 사이여야 합니다.");
        }
    }
}
