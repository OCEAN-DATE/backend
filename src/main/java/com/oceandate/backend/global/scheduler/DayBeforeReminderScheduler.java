package com.oceandate.backend.global.scheduler;

import com.oceandate.backend.domain.matching.entity.OneToOne;
import com.oceandate.backend.domain.matching.entity.Rotation;
import com.oceandate.backend.domain.matching.entity.Travel;
import com.oceandate.backend.domain.matching.repository.OneToOneRepository;
import com.oceandate.backend.domain.matching.repository.RotationRepository;
import com.oceandate.backend.domain.matching.repository.TravelRepository;
import com.oceandate.backend.domain.notification.dto.DayBeforeReminderNotificationRequest;
import com.oceandate.backend.domain.notification.entity.RelatedEntityType;
import com.oceandate.backend.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DayBeforeReminderScheduler {

    private final TravelRepository travelRepository;
    private final RotationRepository rotationRepository;
    private final OneToOneRepository oneToOneRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 9 * * *") // 매일 9시
    public void sendDayBeforeReminders() {
        ZoneId zoneId = ZoneId.of("Asia/Seoul");
        LocalDate tomorrow = LocalDate.now(zoneId).plusDays(1);

        LocalDateTime start = tomorrow.atStartOfDay();
        LocalDateTime end = tomorrow.plusDays(1).atStartOfDay();

        // 1) Travel (eventStartDate=내일)
        for (Travel t : travelRepository.findApprovedByEventStartDate(tomorrow)) {
            notificationService.sendDayBeforeReminder(new DayBeforeReminderNotificationRequest(
                    t.getMember(),
                    t.getEvent().getEventName(),
                    t.getEvent().getEventStartDate().atStartOfDay(),
                    t.getEvent().getLocation(),
                    RelatedEntityType.TRAVEL,
                    t.getId(),
                    null
            ));
        }

        // 2) Rotation (eventDateTime=내일 구간)
        for (Rotation r : rotationRepository.findApprovedByEventDateTimeBetween(start, end)) {
            notificationService.sendDayBeforeReminder(new DayBeforeReminderNotificationRequest(
                    r.getMember(),
                    r.getEvent().getEventName(),
                    r.getEvent().getEventDateTime(),
                    r.getEvent().getLocation(),
                    RelatedEntityType.ROTATION,
                    r.getId(),
                    null
            ));
        }

        // 3) OneToOne (confirmedDate=내일 구간)
        for (OneToOne o : oneToOneRepository.findApprovedByConfirmedDateBetween(start, end)) {
            notificationService.sendDayBeforeReminder(new DayBeforeReminderNotificationRequest(
                    o.getMember(),
                    o.getEvent().getEventName(),
                    o.getConfirmedDate(),
                    o.getEvent().getLocation(),
                    RelatedEntityType.ONE_TO_ONE,
                    o.getId(),
                    null
            ));
        }

        log.info("[ReminderScheduler] day-before reminders done. date={}", tomorrow);
    }
}
