package com.oceandate.backend.domain.matching.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CancelResponse {
    private Long applicationId;
    private Long cancelRequestId;
    private String message;
    private boolean immediate;

    public static CancelResponse pending(Long applicationId, Long cancelRequestId) {
        return CancelResponse.builder()
                .applicationId(applicationId)
                .cancelRequestId(cancelRequestId)
                .message("취소 요청이 접수되었습니다. 관리자 검토 후 처리됩니다.")
                .immediate(false)
                .build();
    }

    public static CancelResponse immediate(Long applicationId) {
        return CancelResponse.builder()
                .applicationId(applicationId)
                .message("취소가 완료되었습니다.")
                .immediate(true)
                .build();
    }
}
