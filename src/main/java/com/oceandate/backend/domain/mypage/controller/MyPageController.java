package com.oceandate.backend.domain.mypage.controller;

import com.oceandate.backend.domain.mypage.dto.MyPageMatchingResponse;
import com.oceandate.backend.domain.mypage.service.MyPageService;
import com.oceandate.backend.domain.payment.dto.MemberCouponResponse;
import com.oceandate.backend.domain.payment.service.CouponService;
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

import java.util.List;

@Tag(name = "MyPage", description = "마이페이지 API")
@Slf4j
@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;
    private final CouponService couponService;

    @Operation(
            summary = "내 매칭 목록 조회",
            description = "로그인한 사용자의 모든 매칭 목록을 조회합니다. " +
                    "각 매칭에 대해 리뷰 작성 가능 여부와 이미 작성한 리뷰 정보를 포함합니다."
    )
    @GetMapping("/matchings")
    public ResponseEntity<List<MyPageMatchingResponse>> getMyMatchings(Authentication authentication) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        Long userId = accountContext.getMemberId();

        log.info("마이페이지 매칭 목록 조회 - userId: {}", userId);

        List<MyPageMatchingResponse> matchings = myPageService.getMyMatchings(userId);
        return ResponseEntity.ok(matchings);
    }

    @Operation(
            summary = "내 전체 쿠폰 목록 조회",
            description = "로그인한 사용자의 모든 쿠폰을 조회합니다. 사용 완료, 만료된 쿠폰도 포함됩니다."
    )
    @GetMapping("/coupons")
    public ResponseEntity<List<MemberCouponResponse>> getMyCoupons(Authentication authentication) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        Long userId = accountContext.getMemberId();

        log.info("마이페이지 전체 쿠폰 목록 조회 - userId: {}", userId);

        List<MemberCouponResponse> coupons = couponService.getMyCoupons(userId);
        return ResponseEntity.ok(coupons);
    }

    @Operation(
            summary = "내 사용 가능한 쿠폰 목록 조회",
            description = "로그인한 사용자의 사용 가능한 쿠폰만 조회합니다. 만료되지 않고 아직 사용하지 않은 쿠폰만 포함됩니다."
    )
    @GetMapping("/coupons/usable")
    public ResponseEntity<List<MemberCouponResponse>> getMyUsableCoupons(Authentication authentication) {
        AccountContext accountContext = (AccountContext) authentication.getPrincipal();
        Long userId = accountContext.getMemberId();

        log.info("마이페이지 사용 가능한 쿠폰 목록 조회 - userId: {}", userId);

        List<MemberCouponResponse> coupons = couponService.getMyUsableCoupons(userId);
        return ResponseEntity.ok(coupons);
    }
}
