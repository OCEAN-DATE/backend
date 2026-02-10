package com.oceandate.backend.domain.matching.controller;

import com.oceandate.backend.domain.matching.dto.*;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.service.OneToOneEventService;
import com.oceandate.backend.domain.matching.service.OneToOneMatchingService;
import com.oceandate.backend.domain.matching.service.OneToOneService;
import com.oceandate.backend.domain.payment.dto.RefundResponse;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.jwt.AccountContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "One To One")
@RestController
@RequestMapping("/api/onetoone")
@RequiredArgsConstructor
public class OneToOneController {

    private final OneToOneService oneToOneService;
    private final OneToOneEventService oneToOneEventService;
    private final OneToOneMatchingService matchingService;
    private final MemberRepository memberRepository;

    @Operation(summary = "일대일 소개팅 신청", description = "소개팅 신청 후 관리자 승인 필요, 바로 결제 X")
    @PostMapping("/applications")
    public ResponseEntity<String> createApplication(
            @RequestBody OneToOneRequest request,
            @AuthenticationPrincipal AccountContext accountContext) {
        Long userId = accountContext.getMemberId();

        Member user = memberRepository.findById(userId).
                orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        oneToOneService.createApplication(userId, request);

        return ResponseEntity.ok("신청이 완료되었습니다.");
    }

    @Operation(summary = "[관리자] 일대일 소개팅 신청 목록 조회", description = "status를 null로 두면 전체 목록 조회")
    @GetMapping("/applications")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OneToOneResponse>> getApplications(
            @RequestParam(required = false) ApplicationStatus status){

        List<OneToOneResponse> applications = oneToOneService.getApplications(status);

        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "[관리자] 일대일 소개팅 신청 상세 조회")
    @GetMapping("/applications/{applicationId}")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OneToOneResponse> getApplicationDetail(
            @PathVariable Long applicationId
    ){
        OneToOneResponse application = oneToOneService.getApplicationDetail(applicationId);
        return ResponseEntity.ok(application);
    }

    @Operation(summary = "[관리자] 일대일 소개팅 신청 상태 변경")
    @PatchMapping("/application/{id}/status")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status){

        oneToOneService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 일대일 소개팅 신청 목록 조회", description = "조회 시 status가 PAYMENT_PENDING이면 결제하기 버튼 활성화")
    @GetMapping("/my")
    public ResponseEntity<List<OneToOneResponse>> getMyApplications(
            @AuthenticationPrincipal AccountContext accountContext) {
        Long userId = accountContext.getMemberId();

        List<OneToOneResponse> response = oneToOneService.getMyApplications(userId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 일대일 소개팅 신청 상세 조회")
    @GetMapping("/my/{applicationId}")
    public ResponseEntity<OneToOneResponse> getMyApplicationDetail(
            @PathVariable Long applicationId,
            @AuthenticationPrincipal AccountContext accountContext) {
        Long userId = accountContext.getMemberId();

        OneToOneResponse response = oneToOneService.getMyApplicationDetail(userId, applicationId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "[관리자] 일대일 소개팅 이벤트 생성", description = "이미지는 선택사항입니다. 이미지를 보내지 않을 경우 필드를 비활성화하거나 제외해주세요.")
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/event", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<OneToOneEvent> createEvent(
            @ModelAttribute OneToOneEventRequest request
    ){
        OneToOneEvent event = oneToOneEventService.createEvent(request);

        return ResponseEntity.ok(event);
    }

    @Operation(summary = "일대일 소개팅 이벤트 목록 조회")
    @GetMapping("/event")
    public ResponseEntity<List<OneToOneEventResponse>> getEvents(
            @RequestParam(required = false) EventStatus status
            ){
        List<OneToOneEventResponse> response = oneToOneEventService.getEvents(status);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일대일 소개팅 이벤트 상세 조회")
    @GetMapping("/events/{eventId}")
    public ResponseEntity<OneToOneEventResponse> getEventDetail(
            @PathVariable Long eventId
    ){
        OneToOneEventResponse response = oneToOneEventService.getEventDetail(eventId);

        return ResponseEntity.ok(response);
    }
    @Operation(summary = "[관리자] 일대일 소개팅 이벤트 삭제")
    //    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/event/{eventId}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long eventId){
        oneToOneEventService.deleteEvent(eventId);
        return ResponseEntity.ok("이벤트가 삭제되었습니다.");
    }

    @Operation(summary = "[관리자] 일대일 소개팅 이벤트 상태 변경")
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/event/{eventId}")
    public ResponseEntity<String> updateEventStatus(
            @PathVariable Long eventId, @RequestParam EventStatus status){
        oneToOneEventService.updateEventStatus(eventId, status);
        return ResponseEntity.ok("이벤트 상태가 변경되었습니다. ");
    }

    @Operation(summary = "[관리자] 일대일 소개팅 매칭")
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/matching")
    public ResponseEntity<String> createMatching(
            @RequestBody MatchingCreateRequest request){
        matchingService.createMatching(request);
        return ResponseEntity.ok("매칭이 완료되었습니다.");
    }

    @Operation(summary = "[관리자] 공통 선호 날짜 조회", description = "남, 여 한 명씩 선택 시 두 신청자의 공통 선호 날짜 반환")
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/preferredDates")
    public ResponseEntity<List<LocalDate>> getPreferredDates(Long maleApplicationId, Long femaleApplicationId){
        List<LocalDate> response = matchingService.getCommonPreferredDates(maleApplicationId, femaleApplicationId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일대일 신청 취소")
    @DeleteMapping("/applications/{applicationId}")
    public ResponseEntity<RefundResponse> cancelApplication(
            @PathVariable Long applicationId,
            @RequestBody(required = false) CancelRequest request,
            @AuthenticationPrincipal AccountContext accountContext
    ) {
        String cancelReason = request != null ? request.getCancelReason() : null;
        RefundResponse response = oneToOneService.cancelApplication(
                applicationId,
                cancelReason,
                accountContext
        );
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일대일 소개팅 이전 신청서 불러오기", description = "이전에 신청한 내역이 있는 경우 가장 최근 신청서를 불러옵니다.")
    @GetMapping("/applications/previous")
    public ResponseEntity<OneToOneResponse> getPreviousApplication(
            @AuthenticationPrincipal AccountContext accountContext
    ){
        OneToOneResponse response = oneToOneService.getPreviousApplication(accountContext);
        return ResponseEntity.ok(response);
    }
}




