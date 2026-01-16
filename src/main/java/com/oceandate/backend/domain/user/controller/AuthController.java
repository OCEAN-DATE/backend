package com.oceandate.backend.domain.user.controller;

import com.oceandate.backend.domain.user.dto.response.IdentifierResponse;
import com.oceandate.backend.domain.user.service.AuthService;
import com.oceandate.backend.domain.user.service.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.oceandate.backend.global.jwt.AccountContext;
import com.oceandate.backend.domain.user.entity.Member;

@Slf4j
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;

    @GetMapping("/me")
    public ResponseEntity<IdentifierResponse> getCurrentUser(
            @AuthenticationPrincipal AccountContext accountContext) {

        if (accountContext == null) {
            return ResponseEntity.status(401).build();
        }

        Member member = memberService.findById(accountContext.getMemberId());
        IdentifierResponse response = IdentifierResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .build();

        return ResponseEntity.ok(response);
    }

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
