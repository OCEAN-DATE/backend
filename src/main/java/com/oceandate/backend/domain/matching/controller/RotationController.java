package com.oceandate.backend.domain.matching.controller;

import com.oceandate.backend.domain.matching.dto.*;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.service.RotationEventService;
import com.oceandate.backend.domain.matching.service.RotationService;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.global.jwt.AccountContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
            @AuthenticationPrincipal AccountContext accountContext) {
        Long userId = accountContext.getMemberId();

        RotationResponse response = rotationService.createApplication(userId, request);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로테이션 소개팅 이벤트 생성(관리자)")
    @PostMapping(value = "/event", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RotationEventResponse> createEvent(
            @ModelAttribute RotationEventRequest rotationEventRequest
    ){
        RotationEventResponse response = rotationEventService.createEvent(rotationEventRequest);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로테이션 소개팅 이벤트별 신청서 목록 조회 (관리자)")
    @GetMapping("/{eventId}/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RotationResponse>> getApplications(
            @PathVariable Long eventId,
            @RequestParam(required = false) ApplicationStatus status
    ){
        List<RotationResponse> response = rotationService.getApplications(eventId, status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로테이션 소개팅 신청서 상태 변경 (관리자)")
    @PatchMapping("applications/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
        @PathVariable Long id, @RequestParam ApplicationStatus status){

        rotationService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 로테이션 소개팅 신청 목록 조회")
    @GetMapping("/my")
    public ResponseEntity<List<RotationResponse>> getMyApplications(
            @AuthenticationPrincipal AccountContext accountContext
    ){
        List<RotationResponse> applications = rotationService.getMyApplications(accountContext);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "로테이션 소개팅 이벤트 목록 조회")
    @GetMapping("/events")
    public ResponseEntity<List<RotationEventResponse>> getEvents(
            @RequestParam(required = false) EventStatus status
    ){
        List<RotationEventResponse> response = rotationService.getEvents(status);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로테이션 소개팅 별 승인된 신청 조회")
    @GetMapping("/{eventId}/approved")
    public ResponseEntity<List<RotationResponse>> getApprovedMembers(
            @PathVariable Long eventId
    ){
        List<RotationResponse> response = rotationService.getApprovedMembers(eventId);
        return ResponseEntity.ok(response);
    }
}
