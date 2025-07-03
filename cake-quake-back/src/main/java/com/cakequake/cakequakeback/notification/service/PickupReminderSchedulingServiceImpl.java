package com.cakequake.cakequakeback.notification.service;

import com.cakequake.cakequakeback.notification.entities.PickupNotification;
import com.cakequake.cakequakeback.notification.repo.PickupNotificationRepository;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.notification.entities.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PickupReminderSchedulingServiceImpl implements PickupReminderSchedulingService {

    private final PickupNotificationRepository pickupNotificationRepository;
    private final NotificationService notificationService;

    @Override
    public void schedulePickupReminder(CakeOrder order) {
        // 픽업 날짜와 시간을 합쳐 LocalDateTime 객체 생성
        LocalDateTime pickupDateTime = LocalDateTime.of(order.getPickupDate(), order.getPickupTime());

        // 알림 전송 시간 계산: 픽업 시간 24시간 전
        LocalDateTime notificationDateTime = pickupDateTime.minusHours(24);

        // 현재 시간보다 알림 시간이 과거라면 스케줄링하지 않음 (이미 지나간 시간)
        if (notificationDateTime.isBefore(LocalDateTime.now())) {
            // 이미 알림 시간이 지났다면, 스케줄링하지 않고 로그만 남깁니다.
            System.err.println("알림 스케줄링 실패: 주문 ID " + order.getOrderId() + "의 알림 시간이 이미 지났습니다. 예정 시간: " + notificationDateTime);
            return;
        }

        // DB에 저장
        PickupNotification scheduledNotification = PickupNotification.builder()
                .order(order)
                .member(order.getMember()) // CakeOrder에 Member가 연관되어 있다고 가정
                .scheduledSendTime(notificationDateTime)
                .status(PickupNotification.NotificationStatus.PENDING)
                .build();

        pickupNotificationRepository.save(scheduledNotification);
        System.out.println("[Scheduler] 픽업 알림 DB 스케줄링 완료: 주문 ID " + order.getOrderId() + ", 알림 예정 시간: " + notificationDateTime);
    }

    @Scheduled(fixedRate = 60000) // 1분 (60000 밀리초)마다 실행
    @Override
    public void processScheduledNotifications() {
        // 현재 시간으로부터 1분 이내에 발송될 예정이거나 이미 지난 알림들을 조회 (안정성을 위해 약간의 여유를 둠)
        List<PickupNotification> notificationsToProcess =
                pickupNotificationRepository.findByScheduledSendTimeBeforeAndStatus(
                        LocalDateTime.now().plusSeconds(10), // 현재 시간보다 10초 뒤까지 포함
                        PickupNotification.NotificationStatus.PENDING
                );

        if (notificationsToProcess.isEmpty()) {
            return;
        }

        System.out.println("[Scheduler] " + notificationsToProcess.size() + "개의 예약 알림 처리 시작...");

        for (PickupNotification notification : notificationsToProcess) {
            try {
                // 알림 발송 로직
                notificationService.sendNotification(
                        notification.getMember().getUid(),
                        "픽업 하루 전입니다 (" + notification.getOrder().getShop().getShopName() + ")",
                        notification.getOrder().getOrderId(),
                        NotificationType.PICKUP_REMINDER
                );
                notification.markAsSent(); // 발송 완료 상태로 변경
                pickupNotificationRepository.save(notification); // DB 업데이트
                System.out.println("[Scheduler] 픽업 알림 발송 완료: 주문 ID " + notification.getOrder().getOrderId() +
                        ", 구매자 UID " + notification.getMember().getUid() +
                        ", 예정 시간: " + notification.getScheduledSendTime());
            } catch (Exception e) {
                System.err.println("[Scheduler] 픽업 알림 발송 실패: 주문 ID " + notification.getOrder().getOrderId() +
                        ", 에러: " + e.getMessage());
            }
        }
        System.out.println("[Scheduler] 예약 알림 처리 완료.");
    }
}
