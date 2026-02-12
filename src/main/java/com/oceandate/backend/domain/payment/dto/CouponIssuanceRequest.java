package com.oceandate.backend.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponIssuanceRequest {

    private Long couponId;
    private List<Long> memberIds;  // 특정 사용자들에게 발급 (null이면 전체 사용자)
    private Boolean isReviewWriters;  // 후기 작성자들에게만 발급 (true면 후기 작성자만)
}
