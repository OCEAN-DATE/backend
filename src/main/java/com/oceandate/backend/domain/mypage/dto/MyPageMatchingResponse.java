package com.oceandate.backend.domain.mypage.dto;

import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.review.dto.ReviewResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyPageMatchingResponse {

    private Long matchingId;
    private MatchingType matchingType;
    private ApplicationStatus status;
    private String eventName;
    private LocalDateTime createdAt;
    private LocalDateTime matchedAt;

    // 리뷰 관련 정보
    private Boolean canWriteReview;  // 리뷰 작성 가능 여부
    private ReviewResponse myReview;  // 내가 작성한 리뷰
}
