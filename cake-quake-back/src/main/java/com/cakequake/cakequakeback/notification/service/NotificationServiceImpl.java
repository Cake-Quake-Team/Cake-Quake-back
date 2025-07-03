package com.cakequake.cakequakeback.notification.service;

import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.notification.dto.NotificationDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.notification.entities.Notification;
import com.cakequake.cakequakeback.notification.entities.NotificationType;
import com.cakequake.cakequakeback.notification.repo.NotificationRepository;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final MemberValidator memberValidator;
    private final MemberRepository memberRepository;

    @Override
    // 알림 생성
    public void sendNotification(Long uid, String content, Long referenceId, NotificationType type) {

        Member member = memberValidator.validateMemberByUid(uid);

        Notification notification = Notification.builder()
                .member(member)
                .content(content)
                .type(type)
                .referenceId(referenceId)
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

    @Override
    // 알림 수동 삭제
    public void deleteNotification(Long notificationId, String currentUserId) {
        // 1. 알림 조회
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NoSuchElementException("알림을 찾을 수 없습니다: " + notificationId));

        // 2. 현재 로그인한 사용자와 알림의 소유자가 일치하는지 확인 (권한 검증)
        // MemberRepository를 통해 userId로 Member 객체를 찾아서 uid를 비교
        Member currentUser = memberRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다: " + currentUserId));

        if (!notification.getMember().getUid().equals(currentUser.getUid())) {
            throw new IllegalArgumentException("이 알림을 삭제할 권한이 없습니다.");
        }

        // 3. 알림 삭제
        notificationRepository.delete(notification);
    }

    @Override
    // 30일 지난 알림 자동 삭제
    public void deleteOldNotifications() {
        // 현재 시간으로부터 30일 전 시간 계산
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        // 30일보다 오래된 알림 삭제
        int deletedCount = notificationRepository.deleteByRegDateBefore(thirtyDaysAgo);
        System.out.println("DEBUG: " + deletedCount + "개의 오래된 알림이 삭제되었습니다. (기준 시간: " + thirtyDaysAgo + ")");
    }
}
