package com.oceandate.backend.domain.review.controller;

import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.review.dto.ReviewCreateRequest;
import com.oceandate.backend.domain.review.dto.ReviewResponse;
import com.oceandate.backend.domain.review.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Review", description = "리뷰 API")
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "리뷰 작성", description = "완료된 매칭에 대해 리뷰를 작성합니다.")
    @PostMapping
    public ResponseEntity<ReviewResponse> createReview(
            @RequestParam Long userId,
            @Valid @RequestBody ReviewCreateRequest request) {
        log.info("리뷰 작성 요청 - userId: {}, matchingType: {}, matchingId: {}",
                userId, request.getMatchingType(), request.getMatchingId());

        ReviewResponse response = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "리뷰 상세 조회", description = "특정 리뷰의 상세 정보를 조회합니다.")
    @GetMapping("/{reviewId}")
    public ResponseEntity<ReviewResponse> getReview(@PathVariable Long reviewId) {
        log.info("리뷰 조회 요청 - reviewId: {}", reviewId);

        ReviewResponse response = reviewService.getReview(reviewId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내가 작성한 리뷰 목록", description = "로그인한 사용자가 작성한 모든 리뷰를 조회합니다.")
    @GetMapping("/my-reviews")
    public ResponseEntity<List<ReviewResponse>> getMyReviews(@RequestParam Long userId) {
        log.info("내가 작성한 리뷰 목록 조회 - userId: {}", userId);

        List<ReviewResponse> reviews = reviewService.getMyReviews(userId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "특정 매칭의 모든 리뷰 조회", description = "특정 매칭에 대해 작성된 모든 리뷰를 조회합니다.")
    @GetMapping("/matching")
    public ResponseEntity<List<ReviewResponse>> getReviewsByMatching(
            @RequestParam MatchingType matchingType,
            @RequestParam Long matchingId) {
        log.info("매칭 리뷰 조회 - matchingType: {}, matchingId: {}", matchingType, matchingId);

        List<ReviewResponse> reviews = reviewService.getReviewsByMatching(matchingType, matchingId);
        return ResponseEntity.ok(reviews);
    }

    @Operation(summary = "리뷰 작성 가능 여부 확인", description = "특정 매칭에 대해 리뷰 작성이 가능한지 확인합니다.")
    @GetMapping("/can-write")
    public ResponseEntity<Map<String, Boolean>> canWriteReview(
            @RequestParam Long userId,
            @RequestParam MatchingType matchingType,
            @RequestParam Long matchingId) {
        log.info("리뷰 작성 가능 여부 확인 - userId: {}, matchingType: {}, matchingId: {}",
                userId, matchingType, matchingId);

        boolean canWrite = reviewService.canWriteReview(userId, matchingType, matchingId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("canWrite", canWrite);

        return ResponseEntity.ok(response);
    }
}
