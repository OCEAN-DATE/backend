package com.oceandate.backend.global.oauth2.userinfo;


import com.oceandate.backend.domain.user.entity.ProviderType;
import com.oceandate.backend.domain.user.entity.Sex;
import org.apache.commons.lang3.StringUtils;

import static com.oceandate.backend.domain.user.entity.ProviderType.KAKAO;
import static com.oceandate.backend.domain.user.entity.Sex.MAN;
import static com.oceandate.backend.domain.user.entity.Sex.WOMAN;

public record KakaoUserInfo(
        String sub,
        String name,
        String gender,
        String email,
        String birthdate,
        String phoneNumber
) implements SocialUserInfo {

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public Sex getGender() {
        if (gender == null || gender.isBlank()) {
            return null;  // null 반환하고 나중에 사용자가 입력
        }

        return StringUtils.equals(gender, "male") ? MAN : WOMAN;
    }

    @Override
    public String getBirth() {
        return birthdate;
    }

    @Override
    public String getProviderId() {
        return sub;
    }

    @Override
    public ProviderType getProviderType() {
        return KAKAO;
    }

    @Override
    public String getPhoneNumber() {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return null;
        }
        String numberBody = phoneNumber.substring(4);
        return String.format("0%s", numberBody);
    }
}
