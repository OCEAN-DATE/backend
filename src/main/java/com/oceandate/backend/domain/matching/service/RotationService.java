package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.RotationResponse;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.dto.RotationRequest;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.enums.VerificationStatus;
import com.oceandate.backend.domain.matching.repository.RotationEventRepository;
import com.oceandate.backend.domain.matching.repository.RotationRepository;
import com.oceandate.backend.domain.user.entity.UserEntity;
import com.oceandate.backend.domain.user.enums.Gender;
import com.oceandate.backend.domain.user.repository.UserRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class RotationService {

    private final RotationRepository rotationRepository;
    private final RotationEventRepository rotationEventRepository;
    private final UserRepository userRepository;

    @Transactional
    public RotationResponse createApplication(
            Long userId,
            RotationRequest request) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        RotationEvent event = rotationEventRepository.findByIdWithLock(request.getEventId())
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        if (event.getStatus() != EventStatus.OPEN) {
            throw new CustomException(ErrorCode.EVENT_CLOSED);
        }

        boolean alreadyApplied = rotationRepository.existsByUserAndEvent(user, event);
        if (alreadyApplied) {
            throw new CustomException(ErrorCode.DUPLICATE_APPLICATION);
        }

        if (Gender.MALE.equals(user.getGender()) && !event.canApproveMale()) {
            throw new CustomException(ErrorCode.MALE_CAPACITY_FULL);
        }
        if (Gender.FEMALE.equals(user.getGender()) && !event.canApproveFemale()) {
            throw new CustomException(ErrorCode.FEMALE_CAPACITY_FULL);
        }

        String orderId = "rotation_" + UUID.randomUUID().toString();

        Rotation application = Rotation.builder()
                .user(user)
                .event(event)
                .job(request.getJob())
                .introduction(request.getIntroduction())
                .orderId(orderId)
                .status(ApplicationStatus.PAYMENT_PENDING)
                .verificationStatus(VerificationStatus.PENDING)
                .documentDeadline(LocalDateTime.now().plusDays(1))
                .refunded(false)
                .build();

        rotationRepository.save(application);

        return RotationResponse.builder()
                .applicationId(application.getId())
                .orderId(orderId)
                .amount(event.getAmount())
                .orderName(event.getEventName())
                .customerEmail(user.getEmail())
                .build();
    }
}
