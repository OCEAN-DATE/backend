package com.oceandate.backend.domain.user.dto.response;

public record IdentifierResponse(
        Long memberId,
        String name
) {
    public static IdentifierResponse of(Long memberId, String name) {
        return new IdentifierResponse(memberId, name);
    }
}