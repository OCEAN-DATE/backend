package com.oceandate.backend.domain.matching.controller;

import com.oceandate.backend.domain.matching.dto.*;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.service.OneToOneEventService;
import com.oceandate.backend.domain.matching.service.OneToOneMatchingService;
import com.oceandate.backend.domain.matching.service.OneToOneService;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<OneToOne> createApplication(
            @RequestBody OneToOneRequest dto,
            @RequestParam Long userId) {

        Member user = memberRepository.findById(userId).
                orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        OneToOne application = oneToOneService.createApplication(
                userId,
                dto
        );

        return ResponseEntity.ok(application);
    }

    @Operation(summary = "일대일 소개팅 신청 목록 조회", description = "status를 null로 두면 전체 목록 조회")
    @GetMapping("/applications")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OneToOneResponse>> getApplications(
            @RequestParam(required = false) ApplicationStatus status){

        List<OneToOneResponse> applications = oneToOneService.getApplications(status);

        return ResponseEntity.ok(applications);
    }

    @Operation(summary = "일대일 소개팅 신청 상태 변경(관리자)")
    @PatchMapping("/application/{id}/status")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status){

        oneToOneService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 일대일 소개팅 신청 내역 조회", description = "조회 시 status가 PAYMENT_PENDING이면 결제하기 버튼 활성화")
    @GetMapping("/my")
    public ResponseEntity<List<OneToOneResponse>> getMyApplications(
            @RequestParam Long userId
    ){
        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<OneToOneResponse> response = oneToOneService.getMyApplications(userId);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "일대일 소개팅 이벤트 생성(관리자)")
    @PostMapping("/event")
    public ResponseEntity<OneToOneEvent> createEvent(
            @RequestBody OneToOneEventRequest request
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

    @Operation(summary = "일대일 소개팅 매칭(관리자)")
//    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/matching")
    public ResponseEntity<String> createMatching(
            @RequestBody MatchingCreateRequest request){
        matchingService.createMatching(request);
        return ResponseEntity.ok("매칭이 완료되었습니다.");
    }
}




