package com.oceandate.backend.domain.admin.repository;

import com.oceandate.backend.domain.admin.entity.Blacklist;
import com.oceandate.backend.domain.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BlacklistRepository extends JpaRepository<Blacklist, Long> {

    Optional<Blacklist> findByMember(Member member);

    boolean existsByMember(Member member);
}
