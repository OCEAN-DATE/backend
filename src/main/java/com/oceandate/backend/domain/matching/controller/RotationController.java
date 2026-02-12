package com.oceandate.backend.domain.matching.controller;

import com.oceandate.backend.domain.matching.dto.*;
import com.oceandate.backend.domain.matching.entity.RotationEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.service.RotationEventService;
import com.oceandate.backend.domain.matching.service.RotationService;
import com.oceandate.backend.domain.payment.dto.RefundResponse;
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

    @Operation(summary = "[관리자] 로테이션 소개팅 이벤트 생성", description = "이미지는 선택사항입니다. 이미지를 보내지 않을 경우 필드를 비활성화하거나 제외해주세요.")
    @PostMapping(value = "/event", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RotationEventResponse> createEvent(
            @ModelAttribute RotationEventRequest rotationEventRequest
    ){
        RotationEventResponse response = rotationEventService.createEvent(rotationEventRequest);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 로테이션 소개팅 이벤트 삭제")
    @DeleteMapping("/event/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteEvent(@PathVariable Long eventId){
        rotationEventService.deleteEvent(eventId);
        return ResponseEntity.ok("이벤트가 삭제되었습니다.");
    }

    @Operation(summary = "[관리자] 로테이션 소개팅 이벤트 상태 변경")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/event/{eventId}")
    public ResponseEntity<String> updateEventStatus(
            @PathVariable Long eventId, @RequestParam EventStatus status){
        rotationEventService.updateEventStaus(eventId, status);
        return ResponseEntity.ok("이벤트 상태가 변경되었습니다.");
    }

    @Operation(summary = "[관리자] 로테이션 소개팅 이벤트별 신청서 목록 조회")
    @GetMapping("/events/{eventId}/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RotationResponse>> getApplications(
            @PathVariable Long eventId,
            @RequestParam(required = false) ApplicationStatus status
    ){
        List<RotationResponse> response = rotationService.getApplications(eventId, status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 로테이션 소개팅 신청서 상세 조회")
    @GetMapping("/applications/{applicationId}")
    @PreAuthorize(("hasRole('ADMIN')"))
    public ResponseEntity<RotationResponse> getApplicationDetail(
            @PathVariable Long applicationId
    ){
        RotationResponse response = rotationService.getApplicationDetail(applicationId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 로테이션 소개팅 신청서 상태 변경")
    @PatchMapping("/applications/{id}/status")
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


    @Operation(summary = "로테이션 소개팅 이벤트 목록 조회", description = "목록 조회 시 imageUrl, reviews는 응답에 포함되지 않습니다.")
    @GetMapping("/events")
    public ResponseEntity<List<RotationEventResponse>> getEvents(
            @RequestParam(required = false) EventStatus status
    ){
        List<RotationEventResponse> response = rotationService.getEvents(status);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로테이션 소개팅 이벤트 상세 조회")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<RotationEventResponse> getEventDetail(
            @PathVariable Long eventId
    ){
        RotationEventResponse response = rotationEventService.getEventDetail(eventId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 로테이션 소개팅 별 승인된 신청 조회")
    @GetMapping("/events/{eventId}/approved")
    public ResponseEntity<List<RotationResponse>> getApprovedMembers(
            @PathVariable Long eventId
    ){
        List<RotationResponse> response = rotationService.getApprovedMembers(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로테이션 신청 취소")
    @DeleteMapping("/events/{eventId}/applications/{applicationId}/cancel")
    public ResponseEntity<RefundResponse> cancelApplication(
            @PathVariable Long eventId,
            @PathVariable Long applicationId,
            @RequestBody CancelRequest request,
            @AuthenticationPrincipal AccountContext accountContext
    ){
        RefundResponse response = rotationService.cancelApplications(eventId, applicationId, request.getCancelReason(), accountContext);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "로테이션 소개팅 이전 신청서 불러오기", description = "이전에 신청한 내역이 있는 경우 가장 최근 신청서를 불러옵니다.")
    @GetMapping("/applications/previous")
    public ResponseEntity<RotationResponse> getPreviousApplication(
            @AuthenticationPrincipal AccountContext accountContext
    ){
        RotationResponse response = rotationService.getPreviousApplication(accountContext);
        return ResponseEntity.ok(response);
    }
}
