package com.oceandate.backend.global.scheduler;

import com.oceandate.backend.domain.matching.entity.Travel;
import com.oceandate.backend.domain.matching.enums.ApplicationStatus;
import com.oceandate.backend.domain.matching.repository.TravelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApplicationCleanupScheduler {

    private final TravelRepository travelRepository;

    /**
     * 24시간 지난 PAYMENT_PENDING 상태의 여행 신청서 자동 삭제
     * 매일 새벽 3시에 실행
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupExpiredTravelApplications() {
        log.info("여행 신청서 자동 삭제 배치 작업 시작");

        LocalDateTime twentyFourHoursAgo = LocalDateTime.now().minusHours(24);

        List<Travel> expiredApplications = travelRepository.findByStatusAndCreatedAtBefore(
                ApplicationStatus.PAYMENT_PENDING,
                twentyFourHoursAgo
        );

        if (!expiredApplications.isEmpty()) {
            travelRepository.deleteAll(expiredApplications);
            log.info("총 {}개의 만료된 여행 신청서 삭제 완료", expiredApplications.size());
        } else {
            log.info("삭제할 만료된 여행 신청서가 없습니다.");
        }
    }
}
