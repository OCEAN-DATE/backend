package com.oceandate.backend.domain.user.dto.response;

import lombok.Builder;

@Builder
public record IdentifierResponse(
        Long memberId,
        String name
) {
    public static IdentifierResponse of(Long memberId, String name) {
        return new IdentifierResponse(memberId, name);
    }
}