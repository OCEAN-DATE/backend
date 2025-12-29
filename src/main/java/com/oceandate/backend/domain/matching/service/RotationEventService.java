package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.RotationEventRequest;
import com.oceandate.backend.domain.matching.dto.RotationEventResponse;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.RotationEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class RotationEventService {

    private final RotationEventRepository rotationEventRepository;

    @Transactional
    public RotationEventResponse createEvent(RotationEventRequest request){
        RotationEvent rotationEvent = RotationEvent.builder()
                .eventName(request.getEventName())
                .eventDateTime(request.getEventDateTime())
                .maleCapacity(request.getMaleCapacity())
                .femaleCapacity(request.getFemaleCapacity())
                .ageRange(request.getAgeRange())
                .location(request.getLocation())
                .amount(request.getAmount())
                .description(request.getDescription())
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
}
