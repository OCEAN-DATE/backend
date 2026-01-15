package com.oceandate.backend.domain.user.controller;

import com.oceandate.backend.domain.user.dto.UserInfoResponse;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.jwt.AccountContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 정보 API")
@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final MemberRepository memberRepository;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getMyInfo(Authentication authentication) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        Long userId = accountContext.getMemberId();

        log.info("사용자 정보 조회 - userId: {}", userId);

        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        UserInfoResponse response = UserInfoResponse.from(member);
        return ResponseEntity.ok(response);
    }
}
