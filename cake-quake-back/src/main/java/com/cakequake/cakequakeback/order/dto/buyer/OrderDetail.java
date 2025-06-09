package com.cakequake.cakequakeback.order.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class OrderDetail {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long orderId;
        private String status;
        private String orderNumber;
        private String reservedAt;    // ISO 포맷 문자열, 예: "2025-05-18T13:00:00"
        private String uname;         // 구매자 이름
        private String phone;         // 구매자 전화번호

        private List<OrderDetailItem> items;
        private Long totalPrice;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderDetailItem {
        private String cname;             // 상품명
        private Integer productCnt;       // 수량
        private Long price;               // 단가
        private String thumbnailImageUrl; // 썸네일 URL
    }
}