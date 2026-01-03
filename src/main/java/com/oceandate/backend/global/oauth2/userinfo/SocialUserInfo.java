package com.oceandate.backend.global.oauth2.userinfo;


import com.oceandate.backend.domain.user.entity.ProviderType;
import com.oceandate.backend.domain.user.entity.Sex;

public interface SocialUserInfo {
    String getName();
    Sex getGender();
    String getEmail();
    String getBirth();
    String getProviderId();
    ProviderType getProviderType();
    String getPhoneNumber();
}
