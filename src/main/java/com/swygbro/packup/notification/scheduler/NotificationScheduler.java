package com.swygbro.packup.notification.scheduler;

import com.swygbro.packup.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationScheduler {

    private final NotificationService notificationService;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul") // 1분마다 00초에 실행
    public void runScheduledNotification() {
        log.debug("[Notification scheduler] per 1min notification check");
        notificationService.checkAndSendNotifications();
    }
}