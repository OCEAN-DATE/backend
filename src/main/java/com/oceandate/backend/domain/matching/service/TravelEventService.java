package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.AccommodationDto;
import com.oceandate.backend.domain.matching.dto.TravelEventRequest;
import com.oceandate.backend.domain.matching.dto.TravelEventResponse;
import com.oceandate.backend.domain.matching.dto.TravelScheduleDto;
import com.oceandate.backend.domain.matching.entity.Accommodation;
import com.oceandate.backend.domain.matching.entity.TravelEvent;
import com.oceandate.backend.domain.matching.entity.TravelSchedule;
import com.oceandate.backend.domain.matching.enums.EventStatus;
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

    // ============= 이벤트 관리 (생성, 수정, 삭제, 상태 변경) =============

    /**
     * 여행 이벤트 생성
     */
    @Transactional
    public TravelEventResponse createEvent(TravelEventRequest request) {
        TravelEvent event = request.toEntity();

        // 숙소 연결
        if (request.getAccommodationIds() != null && !request.getAccommodationIds().isEmpty()) {
            List<Accommodation> accommodations = accommodationRepository.findAllById(request.getAccommodationIds());
            event.setAccommodations(accommodations);
        }

        TravelEvent savedEvent = travelEventRepository.save(event);
        return TravelEventResponse.from(savedEvent);
    }

    /**
     * 여행 이벤트 수정
     */
    @Transactional
    public TravelEventResponse updateEvent(Long eventId, TravelEventRequest request) {
        TravelEvent event = travelEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        event.setEventName(request.getEventName());
        event.setLocation(request.getLocation());
        event.setEventStartDate(request.getEventStartDate());
        event.setEventEndDate(request.getEventEndDate());
        event.setMaleCapacity(request.getMaleCapacity());
        event.setFemaleCapacity(request.getFemaleCapacity());
        event.setAgeRange(request.getAgeRange());
        event.setAmount(request.getAmount());
        event.setDescription(request.getDescription());

        // 숙소 연결 업데이트
        if (request.getAccommodationIds() != null) {
            event.getAccommodations().clear();
            if (!request.getAccommodationIds().isEmpty()) {
                List<Accommodation> accommodations = accommodationRepository.findAllById(request.getAccommodationIds());
                event.setAccommodations(accommodations);
            }
        }

        return TravelEventResponse.from(event);
    }

    /**
     * 여행 이벤트 삭제 (상태를 DELETED로 변경)
     */
    @Transactional
    public void deleteEvent(Long eventId) {
        TravelEvent event = travelEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        event.setStatus(EventStatus.DELETED);
    }

    /**
     * 여행 이벤트 상태 변경
     */
    @Transactional
    public TravelEventResponse updateEventStatus(Long eventId, EventStatus status) {
        TravelEvent event = travelEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        event.setStatus(status);
        return TravelEventResponse.from(event);
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
     * 숙소 생성 (이벤트와 독립적)
     */
    @Transactional
    public Accommodation createAccommodation(AccommodationDto dto) {
        Accommodation accommodation = Accommodation.builder()
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
     * 전체 숙소 목록 조회
     */
    public List<Accommodation> getAllAccommodations() {
        return accommodationRepository.findAll();
    }

    /**
     * 특정 이벤트의 숙소 목록 조회
     */
    public List<Accommodation> getAccommodationsByEvent(Long eventId) {
        TravelEvent event = travelEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        return event.getAccommodations();
    }
}
