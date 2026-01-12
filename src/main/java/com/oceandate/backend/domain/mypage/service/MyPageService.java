package com.oceandate.backend.domain.mypage.service;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.OneToOneMatching;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.MatchingType;
import com.oceandate.backend.domain.matching.repository.OneToOneMatchingRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.matching.repository.RotationRepository;
import com.oceandate.backend.domain.mypage.dto.MyPageMatchingResponse;
import com.oceandate.backend.domain.review.dto.ReviewResponse;
import com.oceandate.backend.domain.review.entity.Review;
import com.oceandate.backend.domain.review.repository.ReviewRepository;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private final OneToOneRepository oneToOneRepository;
    private final RotationRepository rotationRepository;
    private final OneToOneMatchingRepository oneToOneMatchingRepository;
    private final ReviewRepository reviewRepository;
    private final MemberRepository memberRepository;

    /**
     * 내 매칭 목록 조회 (OneToOne + Rotation)
     */
    public List<MyPageMatchingResponse> getMyMatchings(Long memberId) {
        log.info("마이페이지 매칭 목록 조회 - memberId: {}", memberId);

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        List<MyPageMatchingResponse> responses = new ArrayList<>();

        // 1:1 매칭 목록 조회
        List<OneToOne> oneToOnes = oneToOneRepository.findByMemberId(memberId);
        for (OneToOne oneToOne : oneToOnes) {
            responses.add(buildOneToOneResponse(oneToOne, memberId));
        }

        // 로테이션 매칭 목록 조회
        List<Rotation> rotations = rotationRepository.findByMember(member);
        for (Rotation rotation : rotations) {
            responses.add(buildRotationResponse(rotation, memberId));
        }

        // 최신순 정렬
        return responses.stream()
                .sorted(Comparator.comparing(MyPageMatchingResponse::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 1:1 매칭 응답 생성
     */
    private MyPageMatchingResponse buildOneToOneResponse(OneToOne oneToOne, Long memberId) {
        MyPageMatchingResponse.MyPageMatchingResponseBuilder builder = MyPageMatchingResponse.builder()
                .matchingId(oneToOne.getId())
                .matchingType(MatchingType.ONE_TO_ONE)
                .status(oneToOne.getStatus())
                .eventName(oneToOne.getEvent().getEventName())
                .createdAt(oneToOne.getCreatedAt());

        // 매칭된 상대방 정보 조회
        Optional<OneToOneMatching> matchingOpt = oneToOneMatchingRepository.findByApplicationId(oneToOne.getId());
        if (matchingOpt.isPresent()) {
            OneToOneMatching matching = matchingOpt.get();
            builder.matchedAt(matching.getMatchedAt());

            // 상대방 정보 설정
            OneToOne partnerApplication = matching.getMaleApplication().getId().equals(oneToOne.getId())
                    ? matching.getFemaleApplication()
                    : matching.getMaleApplication();

            Member partner = partnerApplication.getMember();
            builder.partnerInfo(MyPageMatchingResponse.MatchedPartnerInfo.builder()
                    .partnerId(partner.getId())
                    .partnerName(partner.getName())
                    .partnerAge(calculateAge(partner.getBirth()))
                    .partnerJob(partnerApplication.getJob())
                    .build());

            // 리뷰 정보 설정
            setReviewInfo(builder, memberId, MatchingType.ONE_TO_ONE, oneToOne.getId(), oneToOne.getStatus());
        }

        return builder.build();
    }

    /**
     * 로테이션 매칭 응답 생성
     */
    private MyPageMatchingResponse buildRotationResponse(Rotation rotation, Long memberId) {
        MyPageMatchingResponse.MyPageMatchingResponseBuilder builder = MyPageMatchingResponse.builder()
                .matchingId(rotation.getId())
                .matchingType(MatchingType.ROTATION)
                .status(rotation.getStatus())
                .eventName(rotation.getEvent().getEventName())
                .createdAt(rotation.getCreatedAt());

        // 로테이션은 여러 명과 만나므로 상대방 정보는 null
        // 리뷰 정보 설정
        setReviewInfo(builder, memberId, MatchingType.ROTATION, rotation.getId(), rotation.getStatus());

        return builder.build();
    }

    /**
     * 리뷰 작성 가능 여부 및 작성한 리뷰 정보 설정
     */
    private void setReviewInfo(
            MyPageMatchingResponse.MyPageMatchingResponseBuilder builder,
            Long memberId,
            MatchingType matchingType,
            Long matchingId,
            ApplicationStatus status) {

        // 리뷰 작성 가능 여부: 상태가 COMPLETED이고 아직 리뷰를 작성하지 않았을 때
        boolean canWriteReview = status == ApplicationStatus.COMPLETED
                && !reviewRepository.existsByWriterIdAndMatchingTypeAndMatchingId(
                memberId, matchingType, matchingId);

        builder.canWriteReview(canWriteReview);

        // 이미 작성한 리뷰가 있으면 포함
        Optional<Review> reviewOpt = reviewRepository.findByWriterIdAndMatchingTypeAndMatchingId(
                memberId, matchingType, matchingId);

        reviewOpt.ifPresent(review -> builder.myReview(ReviewResponse.from(review)));
    }

    /**
     * 나이 계산 (birth 형식: MMDD 또는 YYYYMMDD)
     */
    private Integer calculateAge(String birth) {
        if (birth == null || birth.isEmpty()) {
            return null;
        }

        try {
            int birthYear;
            if (birth.length() == 4) {
                // MMDD 형식인 경우, 연도 추정 불가
                return null;
            } else if (birth.length() == 8) {
                // YYYYMMDD 형식
                birthYear = Integer.parseInt(birth.substring(0, 4));
            } else {
                return null;
            }

            int currentYear = LocalDate.now().getYear();
            return currentYear - birthYear + 1; // 한국 나이
        } catch (NumberFormatException e) {
            log.warn("생년월일 파싱 실패: {}", birth);
            return null;
        }
    }
}
