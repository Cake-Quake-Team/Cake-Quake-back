package com.cakequake.cakequakeback.notification.service;

import com.cakequake.cakequakeback.notification.dto.NotificationDTO;
import com.cakequake.cakequakeback.notification.entities.NotificationType;

import java.util.List;

public interface NotificationService {
    // 알림 생성
    void sendNotification(Long uid, String content, Long referenceId, NotificationType type);

    // 알림 목록 조회
    List<NotificationDTO> getMyNotifications(Long uid);

    // 알림 읽음 표시
    void markAsRead(Long notificationId);

    // 알림 삭제
    void deleteNotification(Long notificationId, String currentUserId);

    // 30일 지난 알림 자동 삭제
    void deleteOldNotifications();
}
