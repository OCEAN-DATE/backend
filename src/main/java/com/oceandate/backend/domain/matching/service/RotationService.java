package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.RotationEventResponse;
import com.oceandate.backend.domain.matching.dto.RotationResponse;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.dto.RotationRequest;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.RotationEventRepository;
import com.oceandate.backend.domain.matching.repository.RotationRepository;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.entity.Sex;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import com.oceandate.backend.global.jwt.AccountContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RotationService {

    private final RotationRepository rotationRepository;
    private final RotationEventRepository rotationEventRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public RotationResponse createApplication(
            Long userId,
            RotationRequest request) {

        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        RotationEvent event = rotationEventRepository.findByIdWithLock(request.getEventId())
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.OPEN) {
            throw new CustomException(ErrorCode.EVENT_CLOSED);
        }

        boolean alreadyApplied = rotationRepository.existsByMemberAndEvent(user, event);
        if (alreadyApplied) {
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }

        if (Sex.MAN.equals(user.getSex()) && !event.canApproveMale()) {
            throw new CustomException(ErrorCode.MALE_CAPACITY_FULL);
        }
        if (Sex.WOMAN.equals(user.getSex()) && !event.canApproveFemale()) {
            throw new CustomException(ErrorCode.FEMALE_CAPACITY_FULL);
        }

        String orderId = "rotation_" + UUID.randomUUID().toString();

        Rotation application = Rotation.builder()
                .member(user)
                .event(event)
                .job(request.getJob())
                .introduction(request.getIntroduction())
                .orderId(orderId)
                .status(ApplicationStatus.PAYMENT_PENDING)
                .build();

        rotationRepository.save(application);

        return RotationResponse.builder()
                .id(application.getId())
                .orderId(orderId)
                .amount(event.getAmount())
                .orderName(event.getEventName())
                .customerEmail(user.getEmail())
                .build();
    }

    @Transactional
    public void updateStatus(Long id, ApplicationStatus status) {
        Rotation application = rotationRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));
        if (status == ApplicationStatus.APPROVED) {
            application.getEvent().incrementApprovedCount(application.getMember().getSex());
            application.setApprovedAt(LocalDateTime.now());
        }
        application.setStatus(status);
    }

    public List<RotationResponse> getApplications(Long eventId, ApplicationStatus status) {
        List<Rotation> applications;
        if(status == null){
            applications = rotationRepository.findByEventId(eventId);
        }
        else {
            applications = rotationRepository.findByEventIdAndStatus(status);
        }
        return applications.stream()
                .map(RotationResponse::from)
                .toList();
    }

    public List<RotationResponse> getMyApplications(AccountContext accountContext) {
        List<Rotation> applications = rotationRepository.findByMemberId(accountContext.getMemberId());

        return applications.stream()
                .map(RotationResponse::from)
                .toList();
    }

    public List<RotationEventResponse> getEvents(EventStatus status) {
        List<RotationEvent> events;
        if(status == null){
            events = rotationEventRepository.findAll();
        }
        else {
            events = rotationEventRepository.findByStatus(status);
        }

        return events.stream()
                .map(RotationEventResponse::from)
                .toList();
    }

    public List<RotationResponse> getApprovedMembers(Long eventId) {
        List<Rotation> applications = rotationRepository.findByEventIdAndApproved(eventId);

        return applications.stream()
                .map(RotationResponse::from)
                .toList();
    }
}
