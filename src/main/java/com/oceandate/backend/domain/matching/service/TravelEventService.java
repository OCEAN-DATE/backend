package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.AccommodationDto;
import com.oceandate.backend.domain.matching.dto.TravelEventResponse;
import com.oceandate.backend.domain.matching.dto.TravelScheduleDto;
import com.oceandate.backend.domain.matching.entity.Accommodation;
import com.oceandate.backend.domain.matching.entity.TravelEvent;
import com.oceandate.backend.domain.matching.entity.TravelSchedule;
import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.matching.repository.AccommodationRepository;
import com.oceandate.backend.domain.matching.repository.TravelEventRepository;
import com.oceandate.backend.domain.matching.repository.TravelScheduleRepository;
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
    private final TravelScheduleRepository travelScheduleRepository;
    private final AccommodationRepository accommodationRepository;
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

    // ============= 프로그램 관리 (일정) =============

    /**
     * 여행 일정 추가
     */
    @Transactional
    public TravelSchedule addSchedule(Long eventId, TravelScheduleDto dto) {
        TravelEvent event = travelEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        TravelSchedule schedule = TravelSchedule.builder()
                .event(event)
                .day(dto.getDay())
                .time(dto.getTime())
                .activity(dto.getActivity())
                .location(dto.getLocation())
                .build();

        return travelScheduleRepository.save(schedule);
    }

    /**
     * 여행 일정 수정
     */
    @Transactional
    public TravelSchedule updateSchedule(Long scheduleId, TravelScheduleDto dto) {
        TravelSchedule schedule = travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        schedule.setDay(dto.getDay());
        schedule.setTime(dto.getTime());
        schedule.setActivity(dto.getActivity());
        schedule.setLocation(dto.getLocation());

        return schedule;
    }

    /**
     * 여행 일정 삭제
     */
    @Transactional
    public void deleteSchedule(Long scheduleId) {
        TravelSchedule schedule = travelScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        travelScheduleRepository.delete(schedule);
    }

    /**
     * 여행 일정 목록 조회
     */
    public List<TravelSchedule> getSchedules(Long eventId) {
        TravelEvent event = travelEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        return travelScheduleRepository.findByEventOrderByDayAscTimeAsc(event);
    }

    // ============= 프로그램 관리 (숙소) =============

    /**
     * 숙소 추가
     */
    @Transactional
    public Accommodation addAccommodation(Long eventId, AccommodationDto dto) {
        TravelEvent event = travelEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        Accommodation accommodation = Accommodation.builder()
                .event(event)
                .name(dto.getName())
                .address(dto.getAddress())
                .checkInTime(dto.getCheckInTime())
                .checkOutTime(dto.getCheckOutTime())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .build();

        return accommodationRepository.save(accommodation);
    }

    /**
     * 숙소 수정
     */
    @Transactional
    public Accommodation updateAccommodation(Long accommodationId, AccommodationDto dto) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        accommodation.setName(dto.getName());
        accommodation.setAddress(dto.getAddress());
        accommodation.setCheckInTime(dto.getCheckInTime());
        accommodation.setCheckOutTime(dto.getCheckOutTime());
        accommodation.setDescription(dto.getDescription());
        accommodation.setImageUrl(dto.getImageUrl());

        return accommodation;
    }

    /**
     * 숙소 삭제
     */
    @Transactional
    public void deleteAccommodation(Long accommodationId) {
        Accommodation accommodation = accommodationRepository.findById(accommodationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND));

        accommodationRepository.delete(accommodation);
    }

    /**
     * 숙소 목록 조회
     */
    public List<Accommodation> getAccommodations(Long eventId) {
        TravelEvent event = travelEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        return accommodationRepository.findByEvent(event);
    }
}
