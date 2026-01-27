package com.oceandate.backend.domain.matching.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserInfo {
    private Long userId;
    private String name;
    private String email;
    private String job;
    private String introduction;
    private String idealType;
    private String hobby;

    public static UserInfo from(OneToOne oneToOne) {
        if(oneToOne == null){
            return null;
        }

        return UserInfo.builder()
                .userId(oneToOne.getMember().getId())
                .name(oneToOne.getMember().getName())
                .email(oneToOne.getMember().getEmail())
                .job(oneToOne.getJob())
                .introduction(oneToOne.getIntroduction())
                .idealType(oneToOne.getIdealType())
                .hobby(oneToOne.getHobby())
                .build();
    }
}
