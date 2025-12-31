package com.oceandate.backend.global.jwt;

import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.global.oauth2.userinfo.SocialUserInfo;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Builder
@Getter
public class AccountContext implements UserDetails, OidcUser, OAuth2User {

    private Long memberId;
    private String email;
    private String password;
    private List<SimpleGrantedAuthority> roles;

    private String oauth2Id;
    private OidcIdToken idToken;
    private OidcUserInfo oidcUserInfo;
    private Map<String, Object> attributes;

    private SocialUserInfo socialUserInfo;
    private String registrationId;

    // OIDC 로그인용 (OpenID Connect)
    public static AccountContext fromOidcUser(
            OidcUser oidcUser,
            SocialUserInfo socialUserInfo,
            String registrationId
    ) {
        if (oidcUser == null) {
            throw new IllegalArgumentException("OidcUser는 null일 수 없습니다.");
        }
        if (socialUserInfo == null) {
            throw new IllegalArgumentException("SocialUserInfo는 null일 수 없습니다.");
        }
        if (socialUserInfo.getEmail() == null || socialUserInfo.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        return AccountContext.builder()
                .oauth2Id(oidcUser.getName())
                .attributes(oidcUser.getAttributes())
                .idToken(oidcUser.getIdToken())
                .oidcUserInfo(oidcUser.getUserInfo())
                .socialUserInfo(socialUserInfo)
                .email(socialUserInfo.getEmail())
                .password(socialUserInfo.getEmail())
                .registrationId(registrationId)
                .roles(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    // 일반 OAuth2 로그인용
    public static AccountContext fromOAuth2User(
            OAuth2User oauth2User,
            SocialUserInfo socialUserInfo,
            String registrationId
    ) {
        if (oauth2User == null) {
            throw new IllegalArgumentException("OAuth2User는 null일 수 없습니다.");
        }
        if (socialUserInfo == null) {
            throw new IllegalArgumentException("SocialUserInfo는 null일 수 없습니다.");
        }
        if (socialUserInfo.getEmail() == null || socialUserInfo.getEmail().isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }

        return AccountContext.builder()
                .oauth2Id(oauth2User.getName())
                .attributes(oauth2User.getAttributes())
                .socialUserInfo(socialUserInfo)
                .email(socialUserInfo.getEmail())
                .password(socialUserInfo.getEmail())
                .registrationId(registrationId)
                .roles(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                // OIDC 필드는 null
                .idToken(null)
                .oidcUserInfo(null)
                .build();
    }

    // JWT 토큰용
    public static AccountContext of(Long memberId, List<SimpleGrantedAuthority> roles) {
        return AccountContext.builder()
                .memberId(memberId)
                .roles(roles)
                .build();
    }

    public void updateMemberInfo(Member member) {
        this.memberId = member.getId();
    }

    @Override
    public String getName() {
        return oauth2Id;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Map<String, Object> getClaims() {
        return Map.of();
    }

    @Override
    public OidcUserInfo getUserInfo() {
        return oidcUserInfo;
    }

    @Override
    public OidcIdToken getIdToken() {
        return idToken;
    }
}