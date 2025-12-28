package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.OneToOneRequest;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.user.entity.UserEntity;
import com.oceandate.backend.domain.user.repository.UserRepository;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OneToOneService {

    private final OneToOneRepository oneToOneRepository;
    private final UserRepository userRepository;

    @Transactional
    public OneToOne createApplication(Long userId, OneToOneRequest request){

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String orderId = "onetoone" + UUID.randomUUID().toString();

        OneToOne application = OneToOne.builder()
                .user(user)
                .preferredDates(request.getPreferredDates())
                .job(request.getJob())
                .status(ApplicationStatus.APPLICATION_SUBMITTED)
                .introduction(request.getIntroduction())
                .location(request.getLocation())
                .orderId(orderId)
                .build();

        return oneToOneRepository.save(application);
    }

    public List<OneToOne> getApplications(String status) {
        if(status == null){
            return oneToOneRepository.findAll();
        }

        return oneToOneRepository.findByStatus(status);
    }

    @Transactional
    public void updateStatus(Long id, ApplicationStatus status) {
        OneToOne application = oneToOneRepository.findById(id)
                .orElseThrow((() -> new IllegalArgumentException("신청 내역을 찾을 수 없습니다.")));

        application.setStatus(status);
    }

    public List<OneToOne> getMyApplications(Long userId) {
        return oneToOneRepository.findByUserId(userId);
    }
}
