package com.oceandate.backend.domain.admin.controller;

import com.oceandate.backend.domain.admin.dto.response.UserListResponse;
import com.oceandate.backend.domain.admin.service.AdminService;
import com.oceandate.backend.domain.matching.dto.AccommodationDto;
import com.oceandate.backend.domain.matching.dto.TravelScheduleDto;
import com.oceandate.backend.domain.matching.entity.Accommodation;
import com.oceandate.backend.domain.matching.entity.Travel;
import com.oceandate.backend.domain.matching.entity.TravelSchedule;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.service.TravelEventService;
import com.oceandate.backend.domain.matching.service.TravelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;
    private final TravelService travelService;
    private final TravelEventService travelEventService;

    @Operation(summary = "[관리자] 유저 리스트 조회", description = "전체 유저 목록을 조회합니다. includeBlacklisted 파라미터로 블랙리스트 포함 여부를 선택할 수 있습니다.")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserListResponse>> getUserList(
            @RequestParam(defaultValue = "false") boolean includeBlacklisted
    ) {
        List<UserListResponse> users = adminService.getUserList(includeBlacklisted);
        return ResponseEntity.ok(users);
    }

    // ============= 여행 소개팅 관리 =============

    @Operation(summary = "[관리자] 여행 소개팅 신청자 목록 조회", description = "eventId와 status 파라미터로 필터링 가능합니다. 둘 다 null이면 전체 조회")
    @GetMapping("/travel/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Travel>> getTravelApplications(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) ApplicationStatus status
    ) {
        List<Travel> applications = travelService.getApplications(eventId, status);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "[관리자] 여행 소개팅 신청 상태 변경", description = "APPROVED로 변경 시 결제 링크 SMS가 자동 전송됩니다")
    @PatchMapping("/travel/applications/{applicationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateTravelApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status
    ) {
        travelService.updateStatus(applicationId, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[관리자] 여행 소개팅 취소", description = "관리자가 여행 소개팅 신청을 취소합니다")
    @DeleteMapping("/travel/applications/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelTravelApplication(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String reason
    ) {
        travelService.cancelApplication(applicationId, reason != null ? reason : "관리자 취소");
        return ResponseEntity.ok().build();
    }

    // ============= 여행 프로그램 관리 (일정) =============

    @Operation(summary = "[관리자] 여행 일정 추가", description = "여행 이벤트에 일정을 추가합니다")
    @PostMapping("/travel/events/{eventId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelSchedule> addTravelSchedule(
            @PathVariable Long eventId,
            @RequestBody TravelScheduleDto dto
    ) {
        TravelSchedule schedule = travelEventService.addSchedule(eventId, dto);
        return ResponseEntity.ok(schedule);
    }

    @Operation(summary = "[관리자] 여행 일정 수정", description = "여행 일정을 수정합니다")
    @PutMapping("/travel/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelSchedule> updateTravelSchedule(
            @PathVariable Long scheduleId,
            @RequestBody TravelScheduleDto dto
    ) {
        TravelSchedule schedule = travelEventService.updateSchedule(scheduleId, dto);
        return ResponseEntity.ok(schedule);
    }

    @Operation(summary = "[관리자] 여행 일정 삭제", description = "여행 일정을 삭제합니다")
    @DeleteMapping("/travel/schedules/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTravelSchedule(@PathVariable Long scheduleId) {
        travelEventService.deleteSchedule(scheduleId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[관리자] 여행 일정 목록 조회", description = "특정 여행 이벤트의 일정 목록을 조회합니다")
    @GetMapping("/travel/events/{eventId}/schedules")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TravelSchedule>> getTravelSchedules(@PathVariable Long eventId) {
        List<TravelSchedule> schedules = travelEventService.getSchedules(eventId);
        return ResponseEntity.ok(schedules);
    }

    // ============= 여행 프로그램 관리 (숙소) =============

    @Operation(summary = "[관리자] 숙소 추가", description = "여행 이벤트에 숙소를 추가합니다")
    @PostMapping("/travel/events/{eventId}/accommodations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Accommodation> addAccommodation(
            @PathVariable Long eventId,
            @RequestBody AccommodationDto dto
    ) {
        Accommodation accommodation = travelEventService.addAccommodation(eventId, dto);
        return ResponseEntity.ok(accommodation);
    }

    @Operation(summary = "[관리자] 숙소 수정", description = "숙소 정보를 수정합니다")
    @PutMapping("/travel/accommodations/{accommodationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Accommodation> updateAccommodation(
            @PathVariable Long accommodationId,
            @RequestBody AccommodationDto dto
    ) {
        Accommodation accommodation = travelEventService.updateAccommodation(accommodationId, dto);
        return ResponseEntity.ok(accommodation);
    }

    @Operation(summary = "[관리자] 숙소 삭제", description = "숙소를 삭제합니다")
    @DeleteMapping("/travel/accommodations/{accommodationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccommodation(@PathVariable Long accommodationId) {
        travelEventService.deleteAccommodation(accommodationId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[관리자] 숙소 목록 조회", description = "특정 여행 이벤트의 숙소 목록을 조회합니다")
    @GetMapping("/travel/events/{eventId}/accommodations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Accommodation>> getAccommodations(@PathVariable Long eventId) {
        List<Accommodation> accommodations = travelEventService.getAccommodations(eventId);
        return ResponseEntity.ok(accommodations);
    }
}
