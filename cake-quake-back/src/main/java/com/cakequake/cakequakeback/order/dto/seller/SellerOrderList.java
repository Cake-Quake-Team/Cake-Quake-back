package com.cakequake.cakequakeback.order.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SellerOrderList {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response{
        /** 조회된 주문 항목 리스트 */
        private List<SellerOrderListItem> orders;

        /** 페이징 정보 */
        private PageInfo pageInfo;

        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class SellerOrderListItem {
            /** 주문 고유 ID */
            private Long orderId;

            /** 주문 번호 (예: "O20250518001") */
            private String orderNumber;

            /** 주문된 상품(케이크) 이름 */
            private String cname;

            /** 주문된 상품 썸네일 이미지 URL */
            private String thumbnailImageUrl;

            /** 픽업 날짜 */
            private LocalDate pickupDate;

            /** 픽업 시간 */
            private LocalTime pickupTime;

            /** 주문 상태 (예: "픽업 예정", "픽업 완료") */
            private String status;

            /** 주문된 상품 수량 */
            private Integer productCnt;

            /** 해당 주문의 총 금액(정수) */
            private Integer OrderTotalPrice;
        }
        @Getter
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class PageInfo {
            /** 현재 페이지 번호 (0부터 시작) */
            private int currentPage;

            /** 전체 페이지 수 */
            private int totalPages;

            /** 전체 요소(주문) 개수 */
            private long totalElements;
        }

    }

}