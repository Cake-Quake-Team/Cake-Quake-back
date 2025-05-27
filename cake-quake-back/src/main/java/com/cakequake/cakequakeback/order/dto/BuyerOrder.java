package com.cakequake.cakequakeback.order.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class BuyerOrder {

    public static class Response {
        private int code;
        private String message;
        private Data data;

        @Getter
        @NoArgsConstructor
        public static class Data {
            private List<OrderSummary> orders;
            private PageInfo pageInfo;
        }

        @Getter
        @NoArgsConstructor
        public static class OrderSummary {
            private Long orderId;
            private String orderNumber;
            private String shopName;
            private Long totalPrice;
            private String status;
            private String orderType;
            private String reservationDate;
            private String reservationTime;
            private List<OrderItemInfo> items;
        }

        @Getter
        @Builder
        public static class OrderItemInfo {
            private String cname;
            private String thumbnailImageUrl;
            private Long price;
            private Integer productCnt;
            private Object options;
        }

        @Getter
        @Builder
        public static class PageInfo {
            private int currentPage;
            private int totalPages;
            private long totalElements;
        }
    }
}

