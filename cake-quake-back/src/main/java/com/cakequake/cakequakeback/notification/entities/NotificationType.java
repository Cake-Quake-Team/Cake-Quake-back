package com.cakequake.cakequakeback.notification.entities;

public enum NotificationType {

    // 구매자 알림
    PICKUP_REMINDER,            // 픽업 하루 전 알림
    RESERVATION_CONFIRMATION,   // 예약 확정 안내
    CANCELLED_ORDER,            // 판매자가 주문 취소 시 알림
    READY_FOR_PICKUP,           // 픽업 준비 완료 알림
    NO_SHOW_CONFIRMATION,       // 노쇼 처리 알림

    // 판매자 알림
    NEW_ORDER,                  // 새로운 주문 발생
    ORDER_CANCELLED_BY_BUYER,   // 구매자가 주문 취소 시 알림

    // 공통 알림
    GENERAL_NOTICE              // 일반 공지
}
