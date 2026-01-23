package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.OneToOneEventRequest;
import com.oceandate.backend.domain.matching.dto.OneToOneEventResponse;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class OneToOneEventService {

    private final OneToOneEventRepository oneToOneEventRepository;
    private final S3Uploader s3Uploader;

    public OneToOneEvent createEvent(OneToOneEventRequest request) {
        String imageUrl = null;
        if(request.getImage() != null && !request.getImage().isEmpty()){
            imageUrl = s3Uploader.upload(request.getImage());
        }

        OneToOneEvent event = OneToOneEvent.builder()
                .eventName(request.getEventName())
                .location(request.getLocation())
                .amount(request.getAmount())
                .description(request.getDescription())
                .s3Url(imageUrl)
                .status(EventStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        oneToOneEventRepository.save(event);

        return event;
    }

    public List<OneToOneEventResponse> getEvents(EventStatus status) {
        List<OneToOneEvent> response;

        if(status == null){
           response = oneToOneEventRepository.findAll();

           return response.stream()
                   .map(OneToOneEventResponse::from)
                   .collect(Collectors.toList());
        }

        response = oneToOneEventRepository.findByStatus(status);

        return response.stream()
                .map(OneToOneEventResponse::from)
                .collect(Collectors.toList());
    }
}
