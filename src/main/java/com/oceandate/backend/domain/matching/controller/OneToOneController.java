package com.oceandate.backend.domain.matching.controller;

import com.oceandate.backend.domain.matching.dto.OneToOneEventRequest;
import com.oceandate.backend.domain.matching.dto.OneToOneRequest;
import com.oceandate.backend.domain.matching.dto.OneToOneResponse;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.service.OneToOneEventService;
import com.oceandate.backend.domain.matching.service.OneToOneService;
import com.oceandate.backend.domain.user.entity.UserEntity;
import com.oceandate.backend.domain.user.repository.UserRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onetoone")
@RequiredArgsConstructor
public class OneToOneController {

    private final OneToOneService oneToOneService;
    private final OneToOneEventService oneToOneEventService;
    private final UserRepository userRepository;

    @PostMapping("/applications")
    public ResponseEntity<OneToOne> createApplication(
            @RequestBody OneToOneRequest dto,
            @RequestParam Long userId) {

        UserEntity user = userRepository.findById(userId).
                orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        OneToOne application = oneToOneService.createApplication(
                userId,
                dto
        );

        return ResponseEntity.ok(application);
    }

    @GetMapping("/applications")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OneToOneResponse>> getApplications(
            Long userId,
            @RequestParam(required = false) ApplicationStatus status){

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<OneToOneResponse> applications = oneToOneService.getApplications(status);

        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/application/{id}/status")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status){

        oneToOneService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<OneToOneResponse>> getMyApplications(
            @RequestParam Long userId
    ){
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<OneToOneResponse> response = oneToOneService.getMyApplications(userId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/event")
    public ResponseEntity<OneToOneEvent> createEvent(
            @RequestBody OneToOneEventRequest request
    ){
        OneToOneEvent event = oneToOneEventService.createEvent(request);

        return ResponseEntity.ok(event);
    }

}




