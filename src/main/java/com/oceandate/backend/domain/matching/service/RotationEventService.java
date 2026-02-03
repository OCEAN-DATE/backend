package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.RotationEventRequest;
import com.oceandate.backend.domain.matching.dto.RotationEventResponse;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.RotationEventRepository;
import com.oceandate.backend.domain.matching.repository.RotationRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class RotationEventService {

    private final RotationRepository rotationRepository;
    private final RotationEventRepository rotationEventRepository;
    private final S3Uploader s3Uploader;

    @Transactional
    public RotationEventResponse createEvent(RotationEventRequest request){
        String imageUrl = null;
        if(request.getImage() != null && !request.getImage().isEmpty()){
            imageUrl = s3Uploader.upload(request.getImage());
        }

        RotationEvent rotationEvent = RotationEvent.builder()
                .eventName(request.getEventName())
                .eventDateTime(request.getEventDateTime())
                .maleCapacity(request.getMaleCapacity())
                .femaleCapacity(request.getFemaleCapacity())
                .ageRange(request.getAgeRange())
                .location(request.getLocation())
                .amount(request.getAmount())
                .description(request.getDescription())
                .s3Url(imageUrl)
                .status(EventStatus.OPEN)
                .build();

        rotationEventRepository.save(rotationEvent);

        return RotationEventResponse.builder()
                .eventId(rotationEvent.getId())
                .eventName(rotationEvent.getEventName())
                .eventDateTime(rotationEvent.getEventDateTime())
                .maleCapacity(rotationEvent.getMaleCapacity())
                .femaleCapacity(rotationEvent.getFemaleCapacity())
                .ageRange(rotationEvent.getAgeRange())
                .location(rotationEvent.getLocation())
                .amount(rotationEvent.getAmount())
                .description(rotationEvent.getDescription())
                .build();
    }

    @Transactional
    public void deleteEvent(Long eventId) {
        RotationEvent event = rotationEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        List<Rotation> applications = rotationRepository.findByEventIdAndStatus(eventId, ApplicationStatus.PAYMENT_COMPLETED);

        if(!applications.isEmpty()){
            throw new CustomException(ErrorCode.INVALID_DELETE_STATUS);
        }

        event.setStatus(EventStatus.DELETED);
        event.setDeletedAt(LocalDateTime.now());
    }

    @Transactional
    public void updateEventStaus(Long eventId, EventStatus status) {
        RotationEvent event = rotationEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        event.setStatus(status);
    }
}
