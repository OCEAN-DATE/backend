package com.oceandate.backend.domain.user.service;

import com.oceandate.backend.global.jwt.AccountContext;
import com.oceandate.backend.global.oauth2.userinfo.KakaoUserInfo;
import com.oceandate.backend.global.oauth2.userinfo.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("========== CustomOAuth2UserService.loadUser() 시작 ==========");

        try {
            // 1. 기본 OAuth2 사용자 정보 로드
            OAuth2User oauth2User = super.loadUser(userRequest);
            log.info("✅ OAuth2User 로드 완료");
            log.info("   Name: {}", oauth2User.getName());
            log.info("   Attributes: {}", oauth2User.getAttributes());

            // 2. 카카오 사용자 정보 추출
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            log.info("   Registration ID: {}", registrationId);

            SocialUserInfo socialUserInfo = extractSocialUserInfo(oauth2User, registrationId);
            log.info("✅ SocialUserInfo 추출 완료");
            log.info("   Email: {}", socialUserInfo.getEmail());
            log.info("   Name: {}", socialUserInfo.getName());
            log.info("   Provider ID: {}", socialUserInfo.getProviderId());

            // 3. AccountContext 생성
            AccountContext accountContext = AccountContext.fromOAuth2User(
                    oauth2User,
                    socialUserInfo,
                    registrationId
            );

            log.info("✅ AccountContext 생성 완료");
            log.info("   Type: {}", accountContext.getClass().getName());
            log.info("========== CustomOAuth2UserService.loadUser() 완료 ==========");

            return accountContext;

        } catch (Exception e) {
            log.error("❌ OAuth2 사용자 정보 로드 실패", e);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_info_error", "사용자 정보 로드 실패: " + e.getMessage(), null),
                    e
            );
        }
    }

    private SocialUserInfo extractSocialUserInfo(OAuth2User oauth2User, String registrationId) {
        Map<String, Object> attributes = oauth2User.getAttributes();

        if ("kakao".equals(registrationId)) {
            return extractKakaoUserInfo(attributes);
        }

        throw new OAuth2AuthenticationException(
                new OAuth2Error("unsupported_provider", "지원하지 않는 OAuth2 Provider: " + registrationId, null)
        );
    }

    private KakaoUserInfo extractKakaoUserInfo(Map<String, Object> attributes) {
        try {
            // 카카오 ID
            String kakaoId = String.valueOf(attributes.get("id"));
            log.debug("카카오 ID: {}", kakaoId);

            // properties에서 닉네임 추출
            Map<String, Object> properties = (Map<String, Object>) attributes.get("properties");
            String nickname = properties != null ? (String) properties.get("nickname") : null;
            log.debug("닉네임: {}", nickname);

            // kakao_account에서 이메일, 성별, 생일 추출
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

            if (kakaoAccount == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("missing_kakao_account", "카카오 계정 정보가 없습니다", null)
                );
            }

            String email = (String) kakaoAccount.get("email");
            String gender = (String) kakaoAccount.get("gender");
            String birthdate = (String) kakaoAccount.get("birthday");
            String phoneNumber = (String) kakaoAccount.get("phone_number");

            log.debug("Email: {}, Gender: {}, Birthday: {}", email, gender, birthdate);

            if (email == null || email.isBlank()) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("missing_email", "이메일 정보가 필요합니다. 카카오 계정 설정을 확인해주세요.", null)
                );
            }

            return new KakaoUserInfo(
                    kakaoId,      // sub (providerId)
                    nickname,     // name
                    gender,       // gender
                    email,        // email
                    birthdate,    // birthdate
                    phoneNumber   // phoneNumber
            );

        } catch (ClassCastException | NullPointerException e) {
            log.error("카카오 사용자 정보 파싱 실패", e);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("invalid_user_info", "카카오 사용자 정보 형식이 올바르지 않습니다", null),
                    e
            );
        }
    }
}