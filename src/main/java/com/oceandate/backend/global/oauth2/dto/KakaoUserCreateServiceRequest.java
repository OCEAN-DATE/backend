package com.oceandate.backend.global.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.oceandate.backend.global.oauth2.userinfo.KakaoUserInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor(access = PROTECTED)
public class KakaoUserCreateServiceRequest {

    private String sub;
    private String name;
    private String gender;
    private String email;
    private String birthdate;

    @JsonProperty(value = "email_verified")
    private Boolean emailVerified;

    @JsonProperty(value = "phone_number")
    private String phoneNumber;

    @JsonProperty(value = "phone_number_verified")
    private Boolean phoneNumberVerified;

    public KakaoUserInfo toKakaoUserInfo() {
        return new KakaoUserInfo(sub, name, gender, email, birthdate, phoneNumber);
    }
}
