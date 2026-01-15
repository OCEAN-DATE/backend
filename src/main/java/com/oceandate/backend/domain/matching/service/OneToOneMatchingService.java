package com.oceandate.backend.domain.matching.service;

import com.oceandate.backend.domain.matching.dto.MatchingCreateRequest;
import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.OneToOneEvent;
import com.oceandate.backend.domain.matching.entity.OneToOneMatching;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.repository.OneToOneEventRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneMatchingRepository;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.user.entity.Sex;
import com.oceandate.backend.global.exception.CustomException;
import com.oceandate.backend.global.exception.constant.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OneToOneMatchingService {

    private final OneToOneMatchingRepository matchingRepository;
    private final OneToOneRepository oneToOneRepository;
    private final OneToOneEventRepository eventRepository;

    @Transactional
    public void createMatching(MatchingCreateRequest request){
        OneToOneEvent event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new CustomException(ErrorCode.EVENT_NOT_FOUND));

        OneToOne maleApplication = oneToOneRepository.findByIdAndEventId(request.getMaleApplicationId(), event.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        OneToOne femaleApplication = oneToOneRepository.findByIdAndEventId(request.getFemaleApplicationId(), event.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if(maleApplication.getMember().getSex() != Sex.MAN){
            throw new CustomException(ErrorCode.GENDER_MISMATCH);
        }

        if(femaleApplication.getMember().getSex() != Sex.WOMAN){
            throw new CustomException(ErrorCode.GENDER_MISMATCH);
        }

        if(matchingRepository.existsByMaleApplicationIdOrFemaleApplicationId(
                maleApplication.getId(), femaleApplication.getId())){
            throw new CustomException(ErrorCode.ALREADY_MATCHED);
        }

        OneToOneMatching matching = OneToOneMatching.builder()
                .event(event)
                .maleApplication(maleApplication)
                .femaleApplication(femaleApplication)
                .build();

        maleApplication.setStatus(ApplicationStatus.MATCHED);
        maleApplication.setConfirmedDate(request.getConfirmedDate());
        femaleApplication.setStatus(ApplicationStatus.MATCHED);
        femaleApplication.setConfirmedDate(request.getConfirmedDate());

        matchingRepository.save(matching);
    }

    public List<LocalDate> getCommonPreferredDates(Long maleApplicationId, Long femaleApplicationId){
        OneToOne maleApplication = oneToOneRepository.findById(maleApplicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        OneToOne femaleApplication = oneToOneRepository.findById(femaleApplicationId)
                .orElseThrow(() -> new CustomException(ErrorCode.APPLICATION_NOT_FOUND));

        if(maleApplication.getMember().getSex() != Sex.MAN){
            throw new CustomException(ErrorCode.GENDER_MISMATCH);
        }

        if(femaleApplication.getMember().getSex() != Sex.WOMAN){
            throw new CustomException(ErrorCode.GENDER_MISMATCH);
        }

        List<LocalDate> malePreferredDates = maleApplication.getPreferredDates();
        List<LocalDate> femalePreferredDates = femaleApplication.getPreferredDates();

        return malePreferredDates.stream()
                .filter(new HashSet<>(femalePreferredDates)::contains)
                .sorted()
                .collect(Collectors.toList());
    }

}
