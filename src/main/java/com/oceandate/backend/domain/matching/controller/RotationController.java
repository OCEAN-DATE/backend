package com.oceandate.backend.domain.matching.controller;

import com.oceandate.backend.domain.matching.dto.RotationEventRequest;
import com.oceandate.backend.domain.matching.dto.RotationEventResponse;
import com.oceandate.backend.domain.matching.dto.RotationRequest;
import com.oceandate.backend.domain.matching.dto.RotationResponse;
import com.oceandate.backend.domain.matching.service.RotationEventService;
import com.oceandate.backend.domain.matching.service.RotationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rotation")
@RequiredArgsConstructor
public class RotationController {

    private final RotationService rotationService;
    private final RotationEventService rotationEventService;

    @PostMapping("/applications")
    public ResponseEntity<RotationResponse> createApplication(
            @RequestParam Long userId,
            @RequestBody RotationRequest request) {

        RotationResponse response = rotationService.createApplication(
                userId, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/event")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RotationEventResponse> createEvent(
            @RequestBody RotationEventRequest rotationEventRequest
    ){
        RotationEventResponse response = rotationEventService.createEvent(rotationEventRequest);

        return ResponseEntity.ok(response);
    }
}
