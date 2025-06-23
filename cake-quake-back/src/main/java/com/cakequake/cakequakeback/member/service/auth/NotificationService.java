package com.cakequake.cakequakeback.member.service.auth;

import com.cakequake.cakequakeback.member.dto.auth.NotificationDTO;
import com.cakequake.cakequakeback.member.entities.NotificationType;

import java.util.List;

public interface NotificationService {
    // 알림 생성
    void sendNotification(Long uid, String content, NotificationType type);

    // 알림 목록 조회
    List<NotificationDTO> getMyNotifications(Long uid);

    // 알림 읽음 표시
    void markAsRead(Long notificationId);
}
