package com.oceandate.backend.domain.user.controller;

import com.oceandate.backend.domain.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestParam Long memberId,  // TODO: JWT에서 memberId 추출
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("로그아웃 요청 - memberId: {}", memberId);
        authService.logout(memberId, request, response);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/withdraw")
    public ResponseEntity<Void> withdraw(
            @RequestParam Long memberId,  // TODO: JWT에서 memberId 추출
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        log.info("회원 탈퇴 요청 - memberId: {}", memberId);
        authService.withdraw(memberId, request, response);
        return ResponseEntity.ok().build();
    }
}
