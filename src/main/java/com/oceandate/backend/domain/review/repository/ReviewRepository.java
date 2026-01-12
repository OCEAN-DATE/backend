package com.oceandate.backend.domain.review.repository;

import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    /**
     * 작성자가 특정 매칭에 대해 작성한 리뷰가 있는지 확인
     */
    boolean existsByWriterIdAndMatchingTypeAndMatchingId(
            Long writerId,
            MatchingType matchingType,
            Long matchingId
    );

    /**
     * 특정 매칭에 대한 리뷰 조회
     */
    Optional<Review> findByWriterIdAndMatchingTypeAndMatchingId(
            Long writerId,
            MatchingType matchingType,
            Long matchingId
    );

    /**
     * 작성자가 작성한 모든 리뷰 조회
     */
    List<Review> findByWriterIdOrderByCreatedAtDesc(Long writerId);

    /**
     * 특정 매칭에 대한 모든 리뷰 조회
     */
    List<Review> findByMatchingTypeAndMatchingId(MatchingType matchingType, Long matchingId);
}
