package com.oceandate.backend.domain.admin.service;

import com.oceandate.backend.domain.admin.dto.response.UserListResponse;
import com.oceandate.backend.domain.admin.entity.Blacklist;
import com.oceandate.backend.domain.admin.repository.BlacklistRepository;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final MemberRepository memberRepository;
    private final BlacklistRepository blacklistRepository;

    /**
     * 유저 리스트 조회
     * @param includeBlacklisted 블랙리스트 포함 여부
     * @return 유저 리스트
     */
    public List<UserListResponse> getUserList(boolean includeBlacklisted) {
        // 전체 회원 조회 (탈퇴하지 않은 회원만)
        List<Member> members = memberRepository.findAll().stream()
                .filter(member -> !member.isDeleted())
                .collect(Collectors.toList());

        // 블랙리스트 정보를 Map으로 조회 (성능 최적화)
        List<Blacklist> blacklists = blacklistRepository.findAll();
        Map<Long, Blacklist> blacklistMap = blacklists.stream()
                .collect(Collectors.toMap(
                        blacklist -> blacklist.getMember().getId(),
                        blacklist -> blacklist
                ));

        // UserListResponse로 변환
        return members.stream()
                .map(member -> {
                    Blacklist blacklist = blacklistMap.get(member.getId());
                    return UserListResponse.of(member, blacklist);
                })
                .filter(response -> includeBlacklisted || !response.getIsBlacklisted())
                .collect(Collectors.toList());
    }
}
