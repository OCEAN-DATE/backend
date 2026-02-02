package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.TravelEventResponse;
import com.oceandate.backend.domain.matching.entity.TravelEvent;
import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.matching.repository.TravelEventRepository;
import com.oceandate.backend.domain.review.dto.ReviewResponse;
import com.oceandate.backend.domain.review.service.ReviewService;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TravelEventService {

    private final TravelEventRepository travelEventRepository;
    private final ReviewService reviewService;

    /**
     * 여행 소개팅 상품 상세 조회 (여행 일정, 숙소 정보, 리뷰 목록 포함)
     */
    public TravelEventResponse getEventDetail(Long eventId) {
        TravelEvent event = travelEventRepository.findByIdWithDetails(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        // 리뷰 목록 조회
        List<ReviewResponse> reviews = reviewService.getReviewsByMatching(
                MatchingType.TRAVEL,
                eventId
        );

        return TravelEventResponse.fromWithReviews(event, reviews);
    }

    /**
     * 모든 여행 이벤트 목록 조회
     */
    public List<TravelEventResponse> getAllEvents() {
        List<TravelEvent> events = travelEventRepository.findAll();
        return events.stream()
                .map(TravelEventResponse::from)
                .toList();
    }
}
