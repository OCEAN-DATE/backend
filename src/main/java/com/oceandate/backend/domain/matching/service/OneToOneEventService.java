package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.OneToOneEventRequest;
import com.oceandate.backend.domain.matching.dto.OneToOneEventResponse;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneEventRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
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

    private final OneToOneRepository oneToOneRepository;
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

    @Transactional
    public void deleteEvent(Long eventId) {
        OneToOneEvent event = oneToOneEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        List<OneToOne> applications = oneToOneRepository.findByEventIdAndStatus(eventId, ApplicationStatus.PAYMENT_COMPLETED);
        event.setStatus(EventStatus.DELETED);
        event.setDeletedAt(LocalDateTime.now());
    }

    @Transactional
    public void updateEventStatus(Long eventId, EventStatus status){
        OneToOneEvent event = oneToOneEventRepository.findById(eventId)
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        event.setStatus(status);
    }
}
