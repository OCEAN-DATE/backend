package com.oceandate.backend.domain.matching.controller;

import com.oceandate.backend.domain.matching.dto.RotationEventRequest;
import com.oceandate.backend.domain.matching.dto.RotationEventResponse;
import com.oceandate.backend.domain.matching.dto.RotationRequest;
import com.oceandate.backend.domain.matching.dto.RotationResponse;
import com.oceandate.backend.domain.matching.service.RotationEventService;
import com.oceandate.backend.domain.matching.service.RotationService;
import com.oceandate.backend.global.jwt.AccountContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Rotation")
@RestController
@RequestMapping("/api/rotation")
@RequiredArgsConstructor
public class RotationController {

    private final RotationService rotationService;
    private final RotationEventService rotationEventService;

    @Operation(summary = "로테이션 소개팅 신청")
    @PostMapping("/applications")
    public ResponseEntity<RotationResponse> createApplication(
            @RequestBody RotationRequest request,
            Authentication authentication) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        Long userId = accountContext.getMemberId();

        RotationResponse response = rotationService.createApplication(userId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로테이션 소개팅 이벤트 생성(관리자)")
    @PostMapping("/event")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RotationEventResponse> createEvent(
            @RequestBody RotationEventRequest rotationEventRequest
    ){
        RotationEventResponse response = rotationEventService.createEvent(rotationEventRequest);

        return ResponseEntity.ok(response);
    }
}
