package com.oceandate.backend.domain.matching.controller;

import com.oceandate.backend.domain.matching.dto.OneToOneRequest;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.service.OneToOneService;
import com.oceandate.backend.domain.user.entity.UserEntity;
import com.oceandate.backend.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/onetoone")
@RequiredArgsConstructor
public class OneToOneController {

    private final OneToOneService oneToOneService;
    private final UserRepository userRepository;

    @PostMapping("/applications")
    public ResponseEntity<OneToOne> createApplication(
            @RequestBody @Valid OneToOneRequest dto,
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
    public ResponseEntity<List<OneToOne>> getApplications(
            Long userId,
            @RequestParam(required = false) String status){

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        List<OneToOne> applications = oneToOneService.getApplications(status);

        return ResponseEntity.ok(applications);
    }

    @PatchMapping("/application/{id}/status")
//    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam ApplicationStatus status){

        oneToOneService.updateStatus(id, status);
    }


}




