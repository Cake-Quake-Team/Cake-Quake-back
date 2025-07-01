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
        // --- 기존 필드 (일반 통계) ---
        //총 주문 건수
        private Long orderTotalCount;
        //완료된 주문 건수(OrderStatus.PICKUP_COMPLETED 기준
        private Long completedOrderCount;
        //취소된 주문 건수(OrderStatus.RESERVATION_CANCELLED 기준)
        private Long cancelledOrderCount;
        //현재 진행 중인 주문 건수(RESERVATION_PENDING, RESERVATION_CONFIRMED, PREPARING, READY_FOR_PICKUP)
        private Long inProgressOrderCount;

        //총 판매 금액
        private Long totalSalesAmount; // 이 필드를 기존의 '총 판매 금액'으로 유지하거나, 아래 totalSalesAmountOverall과 용도 구분이 필요.
        //평균 주문 금액
        private Double averageSalesAmount;

        //통계 조회 시작 날짜
        private LocalDate startDate;
        //통계 조회 종료 날짜
        private LocalDate endDate;
        //통계 생성 시각
        private LocalDateTime generatedAt;

        // 기존 상품 랭킹 (topSellingProducts) - 이제 ProductSalesRanking으로 사용 가능
        private List<ProductSalesRanking> topSellingProducts; // 기존 필드 유지, ProductSalesRanking DTO는 아래 정의

        // 주문 상태별 건수
        private Map<String, Long> orderStatusCounts;


        // --- 새로운 필드 (총 판매량 화면용) --- //

        // 1. 개요 요약 데이터 (화면 상단)
        private Long totalQuantityOverall; // 총 판매량 (선택 기간 내 모든 완료된 아이템의 총 수량)

        // 2. 월별 비교 데이터 (화면의 "저번 달 / 이번 달 총 판매량" 부분)
        private Long currentMonthQuantity;  // 현재 달 (캘린더 기준) 총 판매량
        private Long previousMonthQuantity; // 저번 달 (캘린더 기준) 총 판매량

        // 3. 월별 판매 추이 데이터 (6개월 차트용)
        private List<MonthlySalesData> monthlySalesTrend;

        // 4. 상품별 판매 테이블 데이터 (화면의 "상품명, 판매량, 총매출" 테이블)
        private List<ProductSalesTableItem> productSalesTable;

        // 5. 상품 랭킹 (아쉬운 랭킹)
        private List<ProductRankingItem> lowestRankingProducts; // 아쉬운 랭킹

        // 6. 상품 랭킹(인기 있는 랭킹)
        private List<ProductRankingItem> topRankingProducts; // 인기 랭킹


        // --- 내부 DTO 정의 (새로운 필드에 필요한) ---

        @Getter
        @Builder
        public static class MonthlySalesData {
            private String monthYear; // "YYYY-MM" 형식 (예: "2025-01")
            private Long totalQuantity;
            private Long totalSales;
        }

        @Getter
        @Builder
        public static class ProductSalesTableItem { // 테이블 전용 DTO를 명확히 분리
            private Long cakeId;
            private String cname;
            private Long totalQuantity;
            private Long totalSaleAmount;
        }

        @Getter @Builder
        public static class ProductRankingItem { // 랭킹 전용 DTO
            private Long cakeId;
            private String cname;
        }

        // 기존 ProductSalesRanking은 상위 판매 상품 랭킹에 활용
        @Getter
        @Builder
        public static class ProductSalesRanking{ // 기존 필드 유지
            private Long cakeId;
            private String cname;
            private Long totalQuantity;
            private Long totalSaleAmount;
            private String thumbnailImageUrl;
        }
    }
}