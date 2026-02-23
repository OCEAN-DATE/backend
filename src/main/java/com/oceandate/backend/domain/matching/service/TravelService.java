package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.TravelApplicationResponse;
import com.oceandate.backend.domain.matching.dto.TravelRequest;
import com.oceandate.backend.domain.matching.dto.TravelResponse;
import com.oceandate.backend.domain.matching.entity.Travel;
import com.oceandate.backend.domain.matching.entity.TravelEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.TravelEventRepository;
import com.oceandate.backend.domain.matching.repository.TravelRepository;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.entity.Sex;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import com.oceandate.backend.global.sms.SmsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TravelService {

    private final TravelRepository travelRepository;
    private final TravelEventRepository travelEventRepository;
    private final MemberRepository memberRepository;
    private final SmsService smsService;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * 여행 소개팅 신청 (이전 신청서가 있으면 업데이트)
     */
    @Transactional
    public TravelResponse createApplication(Long userId, TravelRequest request) {
        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 성별 정보 검증
        if (request.getSex() == null) {
            throw new CustomException(ErrorCode.SEX_REQUIRED);
        }

        // 회원 성별 정보 업데이트 (없는 경우)
        user.updateSex(request.getSex());

        TravelEvent event = travelEventRepository.findByIdWithLock(request.getEventId())
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.OPEN) {
            throw new CustomException(ErrorCode.EVENT_CLOSED);
        }

        // 24시간 이내 결제 대기 중인 신청서가 있는지 확인
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);
        Optional<Travel> existingApplication = travelRepository.findByMemberAndEventAndStatusAndCreatedAtAfter(
                user,
                event,
                ApplicationStatus.PAYMENT_PENDING,
                twentyFourHoursAgo
        );

        if (existingApplication.isPresent()) {
            // 24시간 이내 PAYMENT_PENDING 신청서가 있으면 수정
            Travel existing = existingApplication.get();
            existing.setJob(request.getJob());
            existing.setIntroduction(request.getIntroduction());

            return TravelResponse.builder()
                    .applicationId(existing.getId())
                    .orderId(existing.getOrderId())
                    .amount(event.getAmount())
                    .orderName(event.getEventName())
                    .customerEmail(user.getEmail())
                    .build();
        }

        // 결제 완료된 신청이 있는지 확인 (중복 신청 방지)
        boolean hasCompletedApplication = travelRepository.existsByMemberAndEvent(user, event);
        if (hasCompletedApplication) {
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }

        // 새 신청서 생성 - 신청서의 성별로 정원 체크
        if (Sex.MAN.equals(request.getSex()) && !event.canApproveMale()) {
            throw new CustomException(ErrorCode.MALE_CAPACITY_FULL);
        }
        if (Sex.WOMAN.equals(request.getSex()) && !event.canApproveFemale()) {
            throw new CustomException(ErrorCode.FEMALE_CAPACITY_FULL);
        }

        String orderId = "travel_" + UUID.randomUUID().toString();

        Travel application = Travel.builder()
                .member(user)
                .event(event)
                .job(request.getJob())
                .introduction(request.getIntroduction())
                .orderId(orderId)
                .status(ApplicationStatus.PAYMENT_PENDING)
                .build();

        travelRepository.save(application);

        return TravelResponse.builder()
                .applicationId(application.getId())
                .orderId(orderId)
                .amount(event.getAmount())
                .orderName(event.getEventName())
                .customerEmail(user.getEmail())
                .build();
    }

    /**
     * 이전 신청서 조회 (24시간 이내 + PAYMENT_PENDING 상태만)
     */
    @Transactional(readOnly = true)
    public Optional<Travel> getPreviousApplication(Long userId, Long eventId) {
        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        TravelEvent event = travelEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        return travelRepository.findByMemberAndEventAndStatusAndCreatedAtAfter(
                user,
                event,
                ApplicationStatus.PAYMENT_PENDING,
                twentyFourHoursAgo
        );
    }

    /**
     * 신청서 상태 조회
     */
    @Transactional(readOnly = true)
    public Travel getApplication(Long applicationId) {
        return travelRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
    }

    /**
     * 관리자용 - 신청자 목록 조회 (DTO 반환)
     */
    @Transactional(readOnly = true)
    public List<TravelApplicationResponse> getApplications(Long eventId, ApplicationStatus status) {
        List<Travel> applications;

        if (eventId != null) {
            TravelEvent event = travelEventRepository.findById(eventId)
                    .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

            if (status != null) {
                applications = travelRepository.findByEventAndStatusWithMemberAndEvent(event, status);
            } else {
                applications = travelRepository.findByEventWithMemberAndEvent(event);
            }
        } else if (status != null) {
            applications = travelRepository.findByStatusWithMemberAndEvent(status);
        } else {
            applications = travelRepository.findAllWithMemberAndEvent();
        }

        return applications.stream()
                .map(TravelApplicationResponse::from)
                .toList();
    }

    /**
     * 관리자용 - 신청 상태 변경
     */
    @Transactional
    public void updateStatus(Long applicationId, ApplicationStatus status) {
        Travel application = travelRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        // 승인 시 처리
        if (status == ApplicationStatus.APPROVED) {
            TravelEvent event = application.getEvent();

            // 성별별 인원 제한 확인
            Sex memberSex = application.getMember().getSex();
            if (Sex.MAN.equals(memberSex) && !event.canApproveMale()) {
                throw new CustomException(ErrorCode.MALE_CAPACITY_FULL);
            }
            if (Sex.WOMAN.equals(memberSex) && !event.canApproveFemale()) {
                throw new CustomException(ErrorCode.FEMALE_CAPACITY_FULL);
            }

            // 승인 인원 증가
            event.incrementApprovedCount(memberSex);
            application.setApprovedAt(LocalDateTime.now());

            // 승인 시 결제 링크가 포함된 SMS 전송
            String paymentUrl = String.format("%s/payment/%s", frontendUrl, application.getOrderId());
            smsService.sendPaymentLinkSms(
                    application.getMember().getPhoneNumber(),
                    application.getMember().getName(),
                    application.getOrderId(),
                    paymentUrl
            );
        }

        // 상태 변경
        application.setStatus(status);
    }

    /**
     * 관리자용 - 여행 소개팅 취소
     */
    @Transactional
    public void cancelApplication(Long applicationId, String reason) {
        Travel application = travelRepository.findById(applicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        // 승인된 신청인 경우 인원 수 감소
        if (application.getStatus() == ApplicationStatus.APPROVED ||
            application.getStatus() == ApplicationStatus.PAYMENT_COMPLETED) {
            TravelEvent event = application.getEvent();
            event.decrementApprovedCount(application.getMember().getSex());
        }

        // 취소 처리
        application.cancel(reason, 0);
    }
}
