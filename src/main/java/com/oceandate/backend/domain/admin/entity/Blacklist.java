package com.oceandate.backend.domain.admin.entity;

import com.oceandate.backend.domain.user.entity.Member;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Table(name = "blacklist")
@NoArgsConstructor(access = PROTECTED)
public class Blacklist {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private String createdBy;

    @Builder
    private Blacklist(Member member, String reason, String createdBy) {
        this.member = member;
        this.reason = reason;
        this.createdBy = createdBy;
        this.createdAt = LocalDateTime.now();
    }

    public static Blacklist of(Member member, String reason, String createdBy) {
        return Blacklist.builder()
                .member(member)
                .reason(reason)
                .createdBy(createdBy)
                .build();
    }
}
