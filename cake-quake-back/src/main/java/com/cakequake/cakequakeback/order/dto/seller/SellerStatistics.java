package com.cakequake.cakequakeback.order.dto.seller;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class SellerStatistics {

    @Getter
    @Builder
    public static class Response{
        //총 주문 건수
        private Long orderTotalCount;
        //완료된 주문 건수(OrderStatus.PICKUP_COMPLETED 기준
        private Long completedOrderCount;
        //취소된 주문 건수(OrderStatus.RESERVATION_CANCELLED 기준)
        private Long cancelledOrderCount;
        //현재 진행 중인 주문 건수(RESERVATION_PENDING, RESERVATION_CONFIRMED, PREPARING, READY_FOR_PICKUP)
        private Long inProgressOrderCount;

        //총 판매 금액
        private Double totalSalesAmount;
        //평균 주문 금액
        private Double averageSalesAmount;
        //통계 조회 시작 날짜
        private LocalDate startDate;
        //통계 조회 종료 날짜
        private LocalDate endDate;
        //통계 생성 시각
        private LocalDateTime generatedAt;

        private List<ProducrSalesRanking> topSellingProducts;

        private Map<String, Long> orderStatusCounts;

        @Getter
        @Builder
        public static class ProducrSalesRanking{

            private Long cakeId;
            private String cname;
            private Long totalQuantity;
            private Double totalSaleAmount;
            private String thumbnailImageUrl;
        }


    }
}
