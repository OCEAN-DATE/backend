package com.oceandate.backend.domain.review.dto;

import com.oceandate.backend.domain.matching.enums.MatchingType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateRequest {

    @NotNull(message = "매칭 타입은 필수입니다.")
    private MatchingType matchingType;

    @NotNull(message = "매칭 ID는 필수입니다.")
    private Long matchingId;

    @NotNull(message = "평점은 필수입니다.")
    @Min(value = 1, message = "평점은 최소 1점입니다.")
    @Max(value = 5, message = "평점은 최대 5점입니다.")
    private Integer rating;

    @NotBlank(message = "리뷰 내용은 필수입니다.")
    private String content;
}
