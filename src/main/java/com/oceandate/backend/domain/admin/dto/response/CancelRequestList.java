package com.oceandate.backend.domain.admin.dto.response;

import com.oceandate.backend.domain.matching.entity.CancelRequest;
import com.oceandate.backend.domain.matching.enums.CancelRequestStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CancelRequestList {
    private Long cancelRequestId;
    private Long applicationId;
    private Long requesterId;
    private String requesterName;
    private String cancelReason;
    private CancelRequestStatus status;
    private LocalDateTime createdAt;

    public static CancelRequestList of(CancelRequest cr) {
        return CancelRequestList.builder()
                .cancelRequestId(cr.getId())
                .applicationId(cr.getApplication().getId())
                .requesterId(cr.getRequester().getId())
                .requesterName(cr.getRequester().getName())
                .cancelReason(cr.getCancelReason())
                .status(cr.getStatus())
                .createdAt(cr.getCreatedAt())
                .build();
    }
}
