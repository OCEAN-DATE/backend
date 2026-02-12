package com.oceandate.backend.domain.admin.dto.response;

import com.oceandate.backend.domain.admin.entity.Blacklist;
import com.oceandate.backend.domain.user.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserListResponse {

    private Long userId;
    private String email;
    private String name;
    private Integer age;
    private String birth;
    private String sex;
    private String phoneNumber;
    private Boolean isBlacklisted;
    private String blacklistReason;

    public static UserListResponse of(Member member, Blacklist blacklist) {
        return UserListResponse.builder()
                .userId(member.getId())
                .email(member.getEmail())
                .name(member.getName())
                .age(calculateAge(member.getBirth()))
                .birth(member.getBirth())
                .sex(member.getSex() != null ? member.getSex().name() : null)
                .phoneNumber(member.getPhoneNumber())
                .isBlacklisted(blacklist != null)
                .blacklistReason(blacklist != null ? blacklist.getReason() : null)
                .build();
    }

    private static Integer calculateAge(String birth) {
        if (birth == null || birth.isEmpty()) {
            return null;
        }
        try {
            int birthYear = Integer.parseInt(birth.substring(0, 4));
            int currentYear = LocalDate.now().getYear();
            return currentYear - birthYear + 1; // 한국 나이
        } catch (Exception e) {
            return null;
        }
    }
}
