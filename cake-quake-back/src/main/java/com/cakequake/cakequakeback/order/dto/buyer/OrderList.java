package com.cakequake.cakequakeback.order.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class OrderList {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        //조회 주문 목록
        private List<OrderListItem> orders;
        //페이지 정보
        private PageInfo pageInfo;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderListItem {
        private Long orderId;               //주문 id
        private String orderNumber;         //주문 번호
        private String shopName;            //가게 이름
        private Integer orderTotalPrice;    //주문 총 금액
        private String status;              // ex: "주문 확정"
        private String orderType;           //주문 타입
        private LocalDate pickupDate;       //픽업 날짜
        private LocalTime pickupTime;       //픽업 시간

        private List<OrderItemOption> items;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemOption {
        // 일반 케이크 정보
        private String cname;
        private String thumbnailImageUrl;
        private Long price;
        private Integer productCnt;

        // 일반 케이크 옵션 (key-value)
        private Map<String, String> options;
        private Long cakeOptionMappingId;
        private String optionName;
        private Long optionPrice;

        // 커스텀 케이크 정보 (일반 케이크엔 null)
        private String image;         // custom image URL
        private String sheet;         // 시트 정보
        private String innerCream;    // 내부 크림
        private String outerCream;    // 외부 크림
        private String lettering;     // 문구
        private String customRequest; // 커스텀 요청사항
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PageInfo {
        private int currentPage;
        private int totalPages;
        private long totalElements;
    }
}