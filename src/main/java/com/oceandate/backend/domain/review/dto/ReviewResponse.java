package com.oceandate.backend.domain.review.dto;

import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.review.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewResponse {

    private Long id;
    private Long writerId;
    private String writerName;
    private MatchingType matchingType;
    private Long matchingId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    public static ReviewResponse from(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .writerId(review.getWriter().getId())
                .writerName(review.getWriter().getName())
                .matchingType(review.getMatchingType())
                .matchingId(review.getMatchingId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .build();
    }
}
