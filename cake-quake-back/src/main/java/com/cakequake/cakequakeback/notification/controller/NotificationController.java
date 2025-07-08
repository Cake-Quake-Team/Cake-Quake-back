package com.cakequake.cakequakeback.notification.controller;

import com.cakequake.cakequakeback.notification.dto.NotificationDTO;
import com.cakequake.cakequakeback.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
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

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable Long notificationId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            notificationService.deleteNotification(notificationId, userDetails.getUsername());
            return ResponseEntity.ok().build(); // 200 OK 응답
        } catch (IllegalArgumentException e) {
            // 권한이 없거나 알림이 존재하지 않는 경우
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403 Forbidden
        } catch (Exception e) {
            System.err.println("알림 삭제 실패: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // 500 Internal Server Error
        }
    }
}
