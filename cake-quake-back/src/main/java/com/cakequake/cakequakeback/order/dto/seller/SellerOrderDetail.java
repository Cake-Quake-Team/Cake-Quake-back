package com.cakequake.cakequakeback.order.dto.seller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class SellerOrderDetail {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        /** 주문 고유 ID */
        private Long orderId;

        /** 주문 번호 */
        private String orderNumber;

        /** 주문 상태 (예: "픽업 예정", "픽업 완료") */
        private String status;

        /** 픽업 날짜 */
        private LocalDate pickupDate;
        /** 픽업 시간 */
        private LocalTime pickupTime;

        private List<ProductDetail> products;

        private BuyerInfo buyer;

        /** 해당 주문의 총 금액 */
        private Integer OrderTotalPrice;
    }
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductDetail {
        /** CakeItem 엔티티의 name 필드 */
        private String name;

        /** CakeOrderItem 엔티티의 quantity 필드 (주문 수량) */
        private Integer quantity;

        /** CakeOrderItem 엔티티의 unitPrice 필드 (단가) */
        private Integer unitPrice;

        /** CakeOrderItem 엔티티의 subTotalPrice 필드 (소계 금액) */
        private BigDecimal subTotalPrice;

        /** CakeItem 엔티티의 thumbnailImageUrl 필드 (썸네일 URL) */
        private String thumbnailImageUrl;

        /**CakeOrderItemOption 엔티티에서 가져온 “선택된 옵션 매핑 정보”*/
        private Map<String, Integer> options;
    }
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BuyerInfo {
        /** Member 엔티티의 name 필드 */
        private String uname;

        /** Member 엔티티의 phone 필드 */
        private String phoneNumber;
    }
}