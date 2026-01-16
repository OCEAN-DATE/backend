package com.oceandate.backend.domain.user.dto;

import com.oceandate.backend.domain.user.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserInfoResponse {

    private String userId;
    private String role;
    private String name;
    private String email;
    private String phoneNumber;

    public static UserInfoResponse from(Member member) {
        return UserInfoResponse.builder()
                .userId(String.valueOf(member.getId()))
                .role(member.getRole().name().toLowerCase())  // "user" 또는 "admin"
                .name(member.getName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .build();
    }
}
