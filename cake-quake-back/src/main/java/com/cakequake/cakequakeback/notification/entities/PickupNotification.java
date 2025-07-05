package com.cakequake.cakequakeback.notification.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "pickup_notification")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PickupNotification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pickupNotiId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private CakeOrder order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    private Member member;

    @Column(nullable = false)
    private LocalDateTime scheduledSendTime; // ⭐ 알림 발송 예정 시간 ⭐

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status; // ⭐ PENDING, SENT 등의 상태 ⭐

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
    }

    public enum NotificationStatus {
        PENDING,   // 발송 대기 중
        SENT      // 발송 완료
    }
}
