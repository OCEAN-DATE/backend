package com.oceandate.backend.domain.notification.controller;

import com.oceandate.backend.domain.notification.dto.NotificationInboxItemResponse;
import com.oceandate.backend.domain.notification.service.NotificationService;
import com.oceandate.backend.global.jwt.AccountContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notification", description = "알림함 조회 API")
@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "내 알림함 조회",
            description = "로그인한 사용자의 발송 성공(SENT) 알림 목록을 최신순으로 조회합니다. 최대 50개까지 조회 가능합니다."
    )
    @GetMapping
    public ResponseEntity<List<NotificationInboxItemResponse>> getMyNotifications(
            Authentication authentication,
            @RequestParam(defaultValue = "20") int limit
    ) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        Long memberId = accountContext.getMemberId();

        log.info("알림함 조회 - memberId={}, limit={}", memberId, limit);
        return ResponseEntity.ok(notificationService.getInbox(memberId, limit));
    }
}
