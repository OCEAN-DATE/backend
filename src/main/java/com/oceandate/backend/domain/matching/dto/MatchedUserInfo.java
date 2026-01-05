package com.oceandate.backend.domain.matching.dto;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatchedUserInfo {
    private Long userId;
    private String name;
    private String email;
    private String job;
    private String introduction;
    private String idealType;
    private String hobby;

    public static MatchedUserInfo from(OneToOne oneToOne) {
        if(oneToOne == null){
            return null;
        }

        return MatchedUserInfo.builder()
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
