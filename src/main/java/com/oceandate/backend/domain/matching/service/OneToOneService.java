package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.MatchedUserInfo;
import com.oceandate.backend.domain.matching.dto.OneToOneRequest;
import com.oceandate.backend.domain.matching.dto.OneToOneResponse;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.enums.EventStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneEventRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneMatchingRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.user.entity.Member;
import com.oceandate.backend.domain.user.repository.MemberRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OneToOneService {

    private final OneToOneRepository oneToOneRepository;
    private final OneToOneEventRepository oneToOneEventRepository;
    private final OneToOneMatchingRepository matchingRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public OneToOne createApplication(Long userId, OneToOneRequest request){

        Member user = memberRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        OneToOneEvent event = oneToOneEventRepository.findById(request.getEventId())
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        if(event.getStatus() != EventStatus.OPEN){
            throw new CustomException(ErrorCode.EVENT_CLOSED);
        }

        String orderId = "onetoone_" + UUID.randomUUID().toString();

        OneToOne application = OneToOne.builder()
                .member(user)
                .event(event)
                .preferredDates(request.getPreferredDates())
                .job(request.getJob())
                .status(ApplicationStatus.APPLICATION_SUBMITTED)
                .introduction(request.getIntroduction())
                .idealType(request.getIdealType())
                .hobby(request.getHobby())
                .orderId(orderId)
                .amount(event.getAmount())
                .build();

        return oneToOneRepository.save(application);
    }

    public List<OneToOneResponse> getApplications(ApplicationStatus status) {
        List<OneToOne> applications;

        if(status == null){
            applications = oneToOneRepository.findAll();
        }
        else{
            applications = oneToOneRepository.findByStatus(status);
        }

        return applications.stream()
                .map(app -> {
                    MatchedUserInfo matchedPartner = null;
                    if (app.getStatus() == ApplicationStatus.MATCHED) {
                        matchedPartner = getMatchedPartner(app.getId());
                    }
                    return OneToOneResponse.from(app, matchedPartner);
                })
                .collect(Collectors.toList());
    }

    private MatchedUserInfo getMatchedPartner(Long applicationId) {
        return matchingRepository.findByApplicationId(applicationId)
                .map(matching -> {
                    OneToOne partner = matching.getMaleApplication().getId().equals(applicationId)
                            ? matching.getFemaleApplication()
                            : matching.getMaleApplication();
                    return MatchedUserInfo.from(partner);
                })
                .orElse(null);
    }

    @Transactional
    public void updateStatus(Long id, ApplicationStatus status) {
        OneToOne application = oneToOneRepository.findById(id)
                .orElseThrow((() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다.")));

        application.setStatus(status);
    }

    public List<OneToOneResponse> getMyApplications(Long userId) {
        List<OneToOne> applications = oneToOneRepository.findByMemberId(userId);

        return applications.stream()
                .map(OneToOneResponse::from)
                .collect(Collectors.toList());
    }
}
