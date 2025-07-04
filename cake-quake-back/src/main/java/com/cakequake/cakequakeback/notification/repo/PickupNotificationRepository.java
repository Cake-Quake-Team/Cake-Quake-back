package com.cakequake.cakequakeback.notification.repo;

import com.cakequake.cakequakeback.notification.entities.PickupNotification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface PickupNotificationRepository extends JpaRepository<PickupNotification, Long> {
    // 발송 예정 시간이 지났고, 아직 발송되지 않은 알림들을 조회
    List<PickupNotification> findByScheduledSendTimeBeforeAndStatus(LocalDateTime currentTime, PickupNotification.NotificationStatus status);
}
