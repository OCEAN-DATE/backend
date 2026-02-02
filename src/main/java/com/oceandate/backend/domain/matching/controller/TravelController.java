package com.oceandate.backend.domain.matching.controller;

import com.oceandate.backend.domain.matching.dto.TravelEventResponse;
import com.oceandate.backend.domain.matching.dto.TravelRequest;
import com.oceandate.backend.domain.matching.dto.TravelResponse;
import com.oceandate.backend.domain.matching.entity.Travel;
import com.oceandate.backend.domain.matching.service.TravelEventService;
import com.oceandate.backend.domain.matching.service.TravelService;
import com.oceandate.backend.global.jwt.AccountContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Travel")
@RestController
@RequestMapping("/api/travel")
@RequiredArgsConstructor
public class TravelController {

    private final TravelService travelService;
    private final TravelEventService travelEventService;

    @Operation(summary = "여행 소개팅 상품 상세 조회 (여행 일정, 숙소 정보, 리뷰 목록)")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<TravelEventResponse> getEventDetail(@PathVariable Long eventId) {
        TravelEventResponse response = travelEventService.getEventDetail(eventId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "모든 여행 이벤트 목록 조회")
    @GetMapping("/events")
    public ResponseEntity<List<TravelEventResponse>> getAllEvents() {
        List<TravelEventResponse> response = travelEventService.getAllEvents();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "여행 소개팅 신청")
    @PostMapping("/applications")
    public ResponseEntity<TravelResponse> createApplication(
            @RequestBody TravelRequest request,
            Authentication authentication) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        Long userId = accountContext.getMemberId();

        TravelResponse response = travelService.createApplication(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "이전 신청서 조회 (있으면 불러오기)")
    @GetMapping("/applications/previous")
    public ResponseEntity<Travel> getPreviousApplication(
            @RequestParam Long eventId,
            Authentication authentication) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        Long userId = accountContext.getMemberId();

        Optional<Travel> previousApplication = travelService.getPreviousApplication(userId, eventId);

        return previousApplication
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(summary = "신청서 상태 조회")
    @GetMapping("/applications/{applicationId}")
    public ResponseEntity<Travel> getApplication(@PathVariable Long applicationId) {
        Travel application = travelService.getApplication(applicationId);
        return ResponseEntity.ok(application);
    }
}
