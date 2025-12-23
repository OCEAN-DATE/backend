package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.OneToOneRequest;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OneToOneService {

    private final OneToOneRepository oneToOneRepository;

    @Transactional
    public OneToOne createApplication(Long userId, OneToOneRequest request){

        OneToOne application = OneToOne.builder()
                .userId(userId)
                .preferredDates(request.getPreferredDates())
                .introduction(request.getIntroduction())
                .location(request.getLocation())
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
}
