package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 알림 받을 사용자 불러오기
    List<Notification> findByMemberOrderByRegDateDesc(Member member);
}