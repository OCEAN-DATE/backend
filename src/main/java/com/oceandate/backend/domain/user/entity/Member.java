package com.oceandate.backend.domain.user.entity;

import com.oceandate.backend.global.oauth2.userinfo.SocialUserInfo;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.EnumType.STRING;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "member")
@NoArgsConstructor(access = PROTECTED)
public class Member {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Enumerated(STRING)
    private Sex sex;

    private String birth;

    private String phoneNumber;

    @Enumerated(STRING)
    private ProviderType providerType;

    private String providerId;

    @Enumerated(STRING)
    private Role role = Role.USER;

    private LocalDateTime deletedAt;  // 탈퇴 일시 (null이면 활성 회원)

    @Builder
    private Member(String email, String name, Sex sex, String birth,
                   String phoneNumber, ProviderType providerType, String providerId) {
        this.email = email;
        this.name = name;
        this.sex = sex;
        this.birth = birth;
        this.phoneNumber = phoneNumber;
        this.providerType = providerType;
        this.providerId = providerId;
    }

    public static Member from(SocialUserInfo userInfo) {
        return Member.builder()
                .email(userInfo.getEmail())
                .name(userInfo.getName())
                .sex(userInfo.getGender())
                .birth(userInfo.getBirth())
                .phoneNumber(userInfo.getPhoneNumber())
                .providerType(userInfo.getProviderType())
                .providerId(userInfo.getProviderId())
                .build();
    }

    /**
     * 회원 탈퇴 처리 (Soft Delete)
     */
    public void withdraw() {
        this.deletedAt = LocalDateTime.now();
    }

    /**
     * 탈퇴 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}
