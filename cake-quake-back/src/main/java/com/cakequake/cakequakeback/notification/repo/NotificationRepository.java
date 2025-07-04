package com.cakequake.cakequakeback.notification.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.notification.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 알림 받을 사용자 불러오기
    List<Notification> findByMemberOrderByRegDateDesc(Member member);

    // 30일 지난 알림 자동 삭제
    int deleteByRegDateBefore(LocalDateTime dateTime);
}