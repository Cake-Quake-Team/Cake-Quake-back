package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.auth.NotificationDTO;
import com.cakequake.cakequakeback.member.entities.NotificationType;
import com.cakequake.cakequakeback.member.service.auth.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications(@AuthenticationPrincipal (expression = "member.uid") Long uid) {
        List<NotificationDTO> notifications = notificationService.getMyNotifications(uid);
        return ResponseEntity.ok(notifications);
    }

    // 알림 읽음 표시
    @PatchMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test")
    public ResponseEntity<Void> testNotification(@AuthenticationPrincipal (expression = "member.uid") Long uid) {
        notificationService.sendNotification(
                uid,
                "픽업일이 하루 남았습니다!",
                NotificationType.NEW_ORDER // 예시
        );
        return ResponseEntity.ok().build();
    }

}
