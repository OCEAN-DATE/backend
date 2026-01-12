package com.oceandate.backend.domain.review.service;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.matching.repository.RotationRepository;
import com.oceandate.backend.domain.review.dto.ReviewCreateRequest;
import com.oceandate.backend.domain.review.dto.ReviewResponse;
import com.oceandate.backend.domain.review.entity.Review;
import com.oceandate.backend.domain.review.repository.ReviewRepository;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;
    private final OneToOneRepository oneToOneRepository;
    private final RotationRepository rotationRepository;

    /**
     * 리뷰 작성 (매칭 이벤트 자체에 대한 리뷰)
     */
    @Transactional
    public ReviewResponse createReview(Long writerId, ReviewCreateRequest request) {
        log.info("리뷰 작성 시작 - writerId: {}, matchingType: {}, matchingId: {}",
                writerId, request.getMatchingType(), request.getMatchingId());

        // 1. 작성자 조회
        Member writer = memberRepository.findById(writerId)
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다."));

        // 2. 이미 리뷰를 작성했는지 확인
        if (reviewRepository.existsByWriterIdAndMatchingTypeAndMatchingId(
                writerId, request.getMatchingType(), request.getMatchingId())) {
            throw new IllegalStateException("이미 해당 매칭에 대한 리뷰를 작성하셨습니다.");
        }

        // 3. 매칭 타입에 따라 유효성 검증
        validateMatchingAndStatus(writerId, request);

        // 4. 리뷰 생성
        Review review = Review.builder()
                .writer(writer)
                .matchingType(request.getMatchingType())
                .matchingId(request.getMatchingId())
                .rating(request.getRating())
                .content(request.getContent())
                .build();

        review.validateRating();
        Review savedReview = reviewRepository.save(review);

        log.info("리뷰 작성 완료 - reviewId: {}", savedReview.getId());
        return ReviewResponse.from(savedReview);
    }

    /**
     * 매칭 타입에 따라 매칭 상태와 참여자 확인
     */
    private void validateMatchingAndStatus(Long writerId, ReviewCreateRequest request) {
        if (request.getMatchingType() == MatchingType.ONE_TO_ONE) {
            validateOneToOneMatching(writerId, request);
        } else if (request.getMatchingType() == MatchingType.ROTATION) {
            validateRotationMatching(writerId, request);
        } else {
            throw new IllegalArgumentException("지원하지 않는 매칭 타입입니다.");
        }
    }

    /**
     * 1:1 매칭 검증
     */
    private void validateOneToOneMatching(Long writerId, ReviewCreateRequest request) {
        OneToOne oneToOne = oneToOneRepository.findById(request.getMatchingId())
                .orElseThrow(() -> new IllegalArgumentException("매칭 정보를 찾을 수 없습니다."));

        // 매칭 상태 확인
        if (oneToOne.getStatus() != ApplicationStatus.COMPLETED) {
            throw new IllegalStateException("완료된 매칭에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        // 작성자가 해당 매칭의 참여자인지 확인
        if (!oneToOne.getMember().getId().equals(writerId)) {
            throw new IllegalArgumentException("해당 매칭의 참여자만 리뷰를 작성할 수 있습니다.");
        }
    }

    /**
     * 로테이션 매칭 검증
     */
    private void validateRotationMatching(Long writerId, ReviewCreateRequest request) {
        Rotation rotation = rotationRepository.findById(request.getMatchingId())
                .orElseThrow(() -> new IllegalArgumentException("매칭 정보를 찾을 수 없습니다."));

        // 매칭 상태 확인
        if (rotation.getStatus() != ApplicationStatus.COMPLETED) {
            throw new IllegalStateException("완료된 매칭에 대해서만 리뷰를 작성할 수 있습니다.");
        }

        // 작성자가 해당 매칭의 참여자인지 확인
        if (!rotation.getMember().getId().equals(writerId)) {
            throw new IllegalArgumentException("해당 매칭의 참여자만 리뷰를 작성할 수 있습니다.");
        }
    }

    /**
     * 리뷰 상세 조회
     */
    public ReviewResponse getReview(Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));
        return ReviewResponse.from(review);
    }

    /**
     * 내가 작성한 리뷰 목록 조회
     */
    public List<ReviewResponse> getMyReviews(Long memberId) {
        List<Review> reviews = reviewRepository.findByWriterIdOrderByCreatedAtDesc(memberId);
        return reviews.stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 매칭에 대한 모든 리뷰 조회
     */
    public List<ReviewResponse> getReviewsByMatching(MatchingType matchingType, Long matchingId) {
        List<Review> reviews = reviewRepository.findByMatchingTypeAndMatchingId(matchingType, matchingId);
        return reviews.stream()
                .map(ReviewResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 매칭에 대한 리뷰 작성 가능 여부 확인
     */
    public boolean canWriteReview(Long memberId, MatchingType matchingType, Long matchingId) {
        return !reviewRepository.existsByWriterIdAndMatchingTypeAndMatchingId(
                memberId, matchingType, matchingId);
    }
}
