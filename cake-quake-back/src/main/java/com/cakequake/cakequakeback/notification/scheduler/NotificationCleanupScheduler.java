package com.cakequake.cakequakeback.notification.scheduler;

import com.cakequake.cakequakeback.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
// 오래된 알림 자동 삭제
public class NotificationCleanupScheduler {

    private final NotificationService notificationService;

    // 매일 자정(0시 0분 0초)에 실행될 스케줄링 작업
    @Scheduled(cron = "0 0 0 * * ?")
    public void deleteOldNotificationsSchedule() {
        notificationService.deleteOldNotifications();
    }
}