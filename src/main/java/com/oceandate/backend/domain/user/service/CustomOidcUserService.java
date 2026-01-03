package com.oceandate.backend.domain.user.service;

import com.oceandate.backend.global.jwt.AccountContext;
import com.oceandate.backend.global.oauth2.dto.KakaoUserCreateServiceRequest;
import com.oceandate.backend.global.oauth2.userinfo.KakaoUserInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import java.util.Optional;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomOidcUserService extends OidcUserService {

    private final RestClient restClient;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOidcUserService.loadUser() 시작");

        try {
            // 1. 기본 OIDC 사용자 정보 로드
            OidcUser oidcUser = super.loadUser(userRequest);
            log.info("OidcUser 로드 완료: {}", oidcUser.getName());

            // 2. 카카오 추가 정보 조회
            KakaoUserInfo userInfo = getKakaoUserInfo(userRequest);
            log.info("KakaoUserInfo 조회 완료: {}", userInfo.getEmail());

            // 3. AccountContext 생성
            String registrationId = userRequest.getClientRegistration().getRegistrationId();
            AccountContext accountContext = AccountContext.fromOidcUser(oidcUser, userInfo, registrationId);

            log.info("AccountContext 생성 완료 - OAuth2 ID: {}", accountContext.getName());
            return accountContext;

        } catch (Exception e) {
            log.error("OAuth2 사용자 정보 로드 실패", e);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("user_info_error", "사용자 정보를 불러오는 데 실패했습니다.", null),
                    e
            );
        }
    }

    private KakaoUserInfo getKakaoUserInfo(OidcUserRequest userRequest) {
        String accessToken = userRequest.getAccessToken().getTokenValue();
        log.debug("카카오 사용자 정보 조회 시작 - Access Token: {}...", accessToken.substring(0, 10));

        try {
            KakaoUserCreateServiceRequest request = restClient.get()
                    .uri("https://kapi.kakao.com/v1/oidc/userinfo")
                    .header("Authorization", "Bearer " + accessToken)
                    .retrieve()
                    .body(KakaoUserCreateServiceRequest.class);

            if (request == null) {
                log.error("카카오 API 응답이 null입니다.");
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_user_info_response", "카카오 사용자 정보 응답이 비어있습니다.", null)
                );
            }

            KakaoUserInfo userInfo = request.toKakaoUserInfo();
            log.info("카카오 사용자 정보 변환 완료: email={}, providerId={}",
                    userInfo.getEmail(), userInfo.getProviderId());

            return userInfo;

        } catch (Exception e) {
            log.error("카카오 사용자 정보 조회 실패: {}", e.getMessage(), e);
            throw new OAuth2AuthenticationException(
                    new OAuth2Error("kakao_api_error", "카카오 API 호출에 실패했습니다: " + e.getMessage(), null),
                    e
            );
        }
    }
}
