package com.oceandate.backend.domain.user.service;

import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.oauth2.userinfo.SocialUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;

    @Transactional
    public Member registerMember(SocialUserInfo userInfo) {
        // 이메일로 기존 회원 확인
        if (memberRepository.existsByEmail(userInfo.getEmail())) {
            return memberRepository.findByEmail(userInfo.getEmail())
                    .orElseThrow(() -> new IllegalArgumentException("회원 조회 실패"));
        }

        // 신규 회원 등록
        Member newMember = Member.from(userInfo);
        return memberRepository.save(newMember);
    }
}
