package com.oceandate.backend.domain.user.controller;

import com.oceandate.backend.domain.user.dto.response.IdentifierResponse;
import com.oceandate.backend.domain.user.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/token")
public class TokenController {

    private final TokenService tokenService;

    @PostMapping("/reissue")
    public ResponseEntity<IdentifierResponse> reissueAccessToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        IdentifierResponse identifierResponse = tokenService.reissueToken(request, response);
        return ResponseEntity.ok().body(identifierResponse);
    }
}

