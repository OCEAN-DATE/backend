package com.oceandate.backend.domain.admin.controller;

import com.oceandate.backend.domain.admin.dto.request.ProcessCancelRequest;
import com.oceandate.backend.domain.admin.dto.response.CancelRequestList;
import com.oceandate.backend.domain.admin.dto.response.UserListResponse;
import com.oceandate.backend.domain.admin.service.AdminService;
import com.oceandate.backend.domain.matching.dto.AccommodationDto;
import com.oceandate.backend.domain.matching.dto.RotationResponse;
import com.oceandate.backend.domain.matching.dto.TravelApplicationResponse;
import com.oceandate.backend.domain.matching.dto.TravelEventRequest;
import com.oceandate.backend.domain.matching.dto.TravelEventResponse;
import com.oceandate.backend.domain.matching.dto.TravelScheduleDto;
import com.oceandate.backend.domain.matching.entity.Accommodation;
import com.oceandate.backend.domain.matching.entity.CancelRequest;
import com.oceandate.backend.domain.matching.entity.Travel;
import com.oceandate.backend.domain.matching.entity.TravelSchedule;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.CancelRequestRepository;
import com.oceandate.backend.domain.matching.service.OneToOneService;
import com.oceandate.backend.domain.matching.service.RotationService;
import com.oceandate.backend.domain.matching.service.TravelEventService;
import com.oceandate.backend.domain.matching.service.TravelService;
import com.oceandate.backend.global.jwt.AccountContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    private final RotationService rotationService;
    private final OneToOneService oneToOneService;
    private final com.oceandate.backend.domain.payment.service.CouponService couponService;

    @Operation(summary = "[관리자] 유저 리스트 조회", description = "전체 유저 목록을 조회합니다. includeBlacklisted 파라미터로 블랙리스트 포함 여부를 선택할 수 있습니다.")
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserListResponse>> getUserList(
            @RequestParam(defaultValue = "false") boolean includeBlacklisted
    ) {
        List<UserListResponse> users = adminService.getUserList(includeBlacklisted);
        return ResponseEntity.ok(users);
    }

    // ============= 여행 이벤트 관리 (생성, 수정, 삭제, 상태 변경) =============

    @Operation(summary = "[관리자] 여행 이벤트 생성", description = "새로운 여행 이벤트를 생성합니다")
    @PostMapping("/travel/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelEventResponse> createTravelEvent(
            @RequestBody TravelEventRequest request
    ) {
        TravelEventResponse response = travelEventService.createEvent(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 여행 이벤트 수정", description = "여행 이벤트 정보를 수정합니다")
    @PutMapping("/travel/events/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelEventResponse> updateTravelEvent(
            @PathVariable Long eventId,
            @RequestBody TravelEventRequest request
    ) {
        TravelEventResponse response = travelEventService.updateEvent(eventId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 여행 이벤트 삭제", description = "여행 이벤트를 삭제합니다 (상태를 DELETED로 변경)")
    @DeleteMapping("/travel/events/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTravelEvent(@PathVariable Long eventId) {
        travelEventService.deleteEvent(eventId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[관리자] 여행 이벤트 상태 변경", description = "여행 이벤트의 상태를 변경합니다 (OPEN, CLOSED, IN_PROGRESS, COMPLETED, DELETED)")
    @PatchMapping("/travel/events/{eventId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelEventResponse> updateTravelEventStatus(
            @PathVariable Long eventId,
            @RequestParam EventStatus status
    ) {
        TravelEventResponse response = travelEventService.updateEventStatus(eventId, status);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 여행 이벤트 목록 조회", description = "모든 여행 이벤트 목록을 조회합니다")
    @GetMapping("/travel/events")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TravelEventResponse>> getTravelEvents() {
        List<TravelEventResponse> events = travelEventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @Operation(summary = "[관리자] 여행 이벤트 상세 조회", description = "특정 여행 이벤트의 상세 정보를 조회합니다")
    @GetMapping("/travel/events/{eventId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TravelEventResponse> getTravelEventDetail(@PathVariable Long eventId) {
        TravelEventResponse response = travelEventService.getEventDetail(eventId);
        return ResponseEntity.ok(response);
    }

    // ============= 여행 소개팅 관리 =============

    @Operation(summary = "[관리자] 여행 소개팅 신청자 목록 조회", description = "eventId와 status 파라미터로 필터링 가능합니다. 둘 다 null이면 전체 조회")
    @GetMapping("/travel/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TravelApplicationResponse>> getTravelApplications(
            @RequestParam(required = false) Long eventId,
            @RequestParam(required = false) ApplicationStatus status
    ) {
        List<TravelApplicationResponse> applications = travelService.getApplications(eventId, status);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "[관리자] 특정 여행 이벤트의 신청자 목록 조회", description = "특정 여행 이벤트에 신청한 사람들의 목록을 조회합니다. status 파라미터로 필터링 가능")
    @GetMapping("/travel/events/{eventId}/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TravelApplicationResponse>> getTravelEventApplications(
            @PathVariable Long eventId,
            @RequestParam(required = false) ApplicationStatus status
    ) {
        List<TravelApplicationResponse> applications = travelService.getApplications(eventId, status);
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

    // ============= 프로그램 관리 (숙소) - 이벤트와 독립적 =============

    @Operation(summary = "[관리자] 숙소 생성", description = "새로운 숙소를 생성합니다 (이벤트와 독립적)")
    @PostMapping("/accommodations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Accommodation> createAccommodation(
            @RequestBody AccommodationDto dto
    ) {
        Accommodation accommodation = travelEventService.createAccommodation(dto);
        return ResponseEntity.ok(accommodation);
    }

    @Operation(summary = "[관리자] 전체 숙소 목록 조회", description = "등록된 모든 숙소를 조회합니다")
    @GetMapping("/accommodations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Accommodation>> getAllAccommodations() {
        List<Accommodation> accommodations = travelEventService.getAllAccommodations();
        return ResponseEntity.ok(accommodations);
    }

    @Operation(summary = "[관리자] 숙소 수정", description = "숙소 정보를 수정합니다")
    @PutMapping("/accommodations/{accommodationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Accommodation> updateAccommodation(
            @PathVariable Long accommodationId,
            @RequestBody AccommodationDto dto
    ) {
        Accommodation accommodation = travelEventService.updateAccommodation(accommodationId, dto);
        return ResponseEntity.ok(accommodation);
    }

    @Operation(summary = "[관리자] 숙소 삭제", description = "숙소를 삭제합니다")
    @DeleteMapping("/accommodations/{accommodationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteAccommodation(@PathVariable Long accommodationId) {
        travelEventService.deleteAccommodation(accommodationId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[관리자] 특정 이벤트의 숙소 목록 조회", description = "특정 여행 이벤트에 연결된 숙소 목록을 조회합니다")
    @GetMapping("/travel/events/{eventId}/accommodations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Accommodation>> getAccommodationsByEvent(@PathVariable Long eventId) {
        List<Accommodation> accommodations = travelEventService.getAccommodationsByEvent(eventId);
        return ResponseEntity.ok(accommodations);
    }

    // ============= 로테이션 소개팅 관리 =============

    @Operation(summary = "[관리자] 로테이션 소개팅 신청자 목록 조회", description = "eventId와 status 파라미터로 필터링 가능합니다")
    @GetMapping("/rotation/events/{eventId}/applications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RotationResponse>> getRotationApplications(
            @PathVariable Long eventId,
            @RequestParam(required = false) ApplicationStatus status
    ) {
        List<RotationResponse> applications = rotationService.getApplications(eventId, status);
        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "[관리자] 로테이션 소개팅 신청 상세 조회", description = "특정 신청의 상세 정보를 조회합니다")
    @GetMapping("/rotation/applications/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RotationResponse> getRotationApplicationDetail(
            @PathVariable Long applicationId
    ) {
        RotationResponse application = rotationService.getApplicationDetail(applicationId);
        return ResponseEntity.ok(application);
    }

    @Operation(summary = "[관리자] 로테이션 소개팅 신청 상태 변경", description = "APPROVED로 변경 시 결제 링크 SMS가 자동 전송됩니다")
    @PatchMapping("/rotation/applications/{applicationId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateRotationApplicationStatus(
            @PathVariable Long applicationId,
            @RequestParam ApplicationStatus status
    ) {
        rotationService.updateStatus(applicationId, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[관리자] 로테이션 소개팅 취소", description = "관리자가 로테이션 소개팅 신청을 취소합니다")
    @DeleteMapping("/rotation/applications/{applicationId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> cancelRotationApplication(
            @PathVariable Long applicationId,
            @RequestParam(required = false) String reason
    ) {
        rotationService.cancelApplication(applicationId, reason != null ? reason : "관리자 취소");
        return ResponseEntity.ok().build();
    }

    // ============= 쿠폰 관리 =============

    @Operation(summary = "[관리자] 쿠폰 생성", description = "새로운 쿠폰을 생성합니다")
    @PostMapping("/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.oceandate.backend.domain.payment.dto.CouponResponse> createCoupon(
            @RequestBody com.oceandate.backend.domain.payment.dto.CouponRequest request
    ) {
        com.oceandate.backend.domain.payment.dto.CouponResponse response = couponService.createCoupon(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 쿠폰 수정", description = "쿠폰 정보를 수정합니다")
    @PutMapping("/coupons/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.oceandate.backend.domain.payment.dto.CouponResponse> updateCoupon(
            @PathVariable Long couponId,
            @RequestBody com.oceandate.backend.domain.payment.dto.CouponRequest request
    ) {
        com.oceandate.backend.domain.payment.dto.CouponResponse response = couponService.updateCoupon(couponId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 쿠폰 활성화/비활성화", description = "쿠폰의 활성화 상태를 토글합니다")
    @PatchMapping("/coupons/{couponId}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> toggleCouponStatus(@PathVariable Long couponId) {
        couponService.toggleCouponStatus(couponId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[관리자] 쿠폰 목록 조회", description = "쿠폰 목록을 조회합니다. isActive 파라미터로 필터링 가능")
    @GetMapping("/coupons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.oceandate.backend.domain.payment.dto.CouponResponse>> getCoupons(
            @RequestParam(required = false) Boolean isActive
    ) {
        List<com.oceandate.backend.domain.payment.dto.CouponResponse> coupons = couponService.getCoupons(isActive);
        return ResponseEntity.ok(coupons);
    }

    @Operation(summary = "[관리자] 쿠폰 상세 조회", description = "특정 쿠폰의 상세 정보를 조회합니다")
    @GetMapping("/coupons/{couponId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.oceandate.backend.domain.payment.dto.CouponResponse> getCouponById(
            @PathVariable Long couponId
    ) {
        com.oceandate.backend.domain.payment.dto.CouponResponse response = couponService.getCouponById(couponId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 특정 사용자에게 쿠폰 발급", description = "특정 사용자에게 쿠폰을 발급합니다")
    @PostMapping("/coupons/{couponId}/issue/{memberId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<com.oceandate.backend.domain.payment.dto.MemberCouponResponse> issueCouponToUser(
            @PathVariable Long couponId,
            @PathVariable Long memberId
    ) {
        com.oceandate.backend.domain.payment.dto.MemberCouponResponse response =
                couponService.issueCouponToUser(couponId, memberId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 여러 사용자에게 쿠폰 발급",
               description = "여러 사용자에게 쿠폰을 발급합니다. " +
                       "isReviewWriters=true면 후기 작성자들에게만 발급, " +
                       "memberIds 지정 시 특정 사용자들에게 발급, " +
                       "둘 다 없으면 전체 사용자에게 발급")
    @PostMapping("/coupons/issue")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<com.oceandate.backend.domain.payment.dto.MemberCouponResponse>> issueCouponToUsers(
            @RequestBody com.oceandate.backend.domain.payment.dto.CouponIssuanceRequest request
    ) {
        List<com.oceandate.backend.domain.payment.dto.MemberCouponResponse> responses =
                couponService.issueCouponToUsers(request);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "[관리자] 일대일 소개팅 취소 신청 목록 조회", description = "일대일 소개팅 매칭 후 취소 시 관리자 승인 필요")
    @GetMapping("/onetoone/cancel-requests")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CancelRequestList>> getCancelRequests(){
        List<CancelRequestList> responses = oneToOneService.getPendingRequests();

        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "[관리자] 일대일 소개팅 취소 승인")
    @PatchMapping("/onetoone/cancel-requests/{cancelRequestId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> approveCancelRequest(
            @PathVariable Long cancelRequestId,
            @AuthenticationPrincipal AccountContext accountContext
            ){
        oneToOneService.approveCancelRequest(cancelRequestId, accountContext);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "[관리자] 일대일 소개팅 취소 거절")
    @PatchMapping("/onetoone/cancel-requests/{cancelRequestId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> rejectCancelRequest(
            @PathVariable Long cancelRequestId,
            @RequestBody ProcessCancelRequest request
    ){
        oneToOneService.rejectCancelRequest(cancelRequestId, request.getAdminComment());
        return ResponseEntity.ok().build();
    }
}
