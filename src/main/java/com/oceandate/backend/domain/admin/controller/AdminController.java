package com.oceandate.backend.domain.admin.controller;

import com.oceandate.backend.domain.admin.dto.response.UserListResponse;
import com.oceandate.backend.domain.admin.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    /**
     * 유저 리스트 조회
     * @param includeBlacklisted 블랙리스트 포함 여부 (기본값: false)
     * @return 유저 리스트
     */
    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserListResponse>> getUserList(
            @RequestParam(defaultValue = "false") boolean includeBlacklisted
    ) {
        List<UserListResponse> users = adminService.getUserList(includeBlacklisted);
        return ResponseEntity.ok(users);
    }
}
