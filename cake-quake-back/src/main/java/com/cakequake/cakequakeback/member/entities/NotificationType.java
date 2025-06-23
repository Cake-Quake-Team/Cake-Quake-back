package com.cakequake.cakequakeback.member.entities;

public enum NotificationType {

    // 구매자 알림
    PICKUP_REMINDER,      // 픽업 하루 전 알림
    ORDER_CONFIRMATION,   // 주문 완료 안내

    // 판매자 알림
    NEW_ORDER,            // 새로운 주문 발생
    CANCELLED_ORDER,      // 주문 취소됨

    // 공통 알림
    GENERAL_NOTICE        // 일반 공지
}
