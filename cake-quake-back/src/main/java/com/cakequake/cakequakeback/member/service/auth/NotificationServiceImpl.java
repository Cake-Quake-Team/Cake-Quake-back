package com.cakequake.cakequakeback.member.service.auth;

import com.cakequake.cakequakeback.member.dto.auth.NotificationDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.Notification;
import com.cakequake.cakequakeback.member.entities.NotificationType;
import com.cakequake.cakequakeback.member.repo.NotificationRepository;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberValidator memberValidator;

    @Override
    // 알림 생성
    public void sendNotification(Long uid, String content, NotificationType type) {

        Member member = memberValidator.validateMemberByUid(uid);

        Notification notification = Notification.builder()
                .member(member)
                .content(content)
                .type(type)
                .build();

        notificationRepository.save(notification);
    }

    @Override
    @Transactional(readOnly = true)
    // 알림 목록 조회
    public List<NotificationDTO> getMyNotifications(Long uid) {
        Member member = memberValidator.validateMemberByUid(uid);
        List<Notification> notifications = notificationRepository.findByMemberOrderByRegDateDesc(member);
        return notifications.stream()
                .map(NotificationDTO::new)
                .toList();
    }

    @Override
    // 알림 읽음 표시
    public void markAsRead(Long notificationId) {
        Notification noti = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("알림 없음"));
        noti.markAsRead();
    }
}
