package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.RotationEventRequest;
import com.oceandate.backend.domain.matching.dto.RotationEventResponse;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.RotationEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class RotationEventService {

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
}
