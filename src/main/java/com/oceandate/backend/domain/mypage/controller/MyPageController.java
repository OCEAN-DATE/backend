package com.oceandate.backend.domain.mypage.controller;

import com.oceandate.backend.domain.mypage.dto.MyPageMatchingResponse;
import com.oceandate.backend.domain.mypage.service.MyPageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "MyPage", description = "마이페이지 API")
@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    @Operation(
            summary = "내 매칭 목록 조회",
            description = "로그인한 사용자의 모든 매칭 목록을 조회합니다. " +
                    "각 매칭에 대해 리뷰 작성 가능 여부와 이미 작성한 리뷰 정보를 포함합니다."
    )
    @GetMapping("/matchings")
    public ResponseEntity<List<MyPageMatchingResponse>> getMyMatchings(
            @RequestParam Long userId) {
        log.info("마이페이지 매칭 목록 조회 - userId: {}", userId);

        List<MyPageMatchingResponse> matchings = myPageService.getMyMatchings(userId);
        return ResponseEntity.ok(matchings);
    }
}
