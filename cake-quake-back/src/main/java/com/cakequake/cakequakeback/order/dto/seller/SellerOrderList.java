package com.cakequake.cakequakeback.order.dto.seller;

import com.cakequake.cakequakeback.order.entities.OrderStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

/** 판매자 주문 받은 상품 목록 */
public class SellerOrderList {

    @Getter
    @Builder
    private static class Response {
        private Long orderId;
        private String cname;
        private String thumbnailUrl;
        private OrderStatus status; // 주문 상태
        private Integer totalNumber;
        private Integer orderTotalPrice;

        private Response(Long orderId, String cname, String thumbnailUrl,
                         OrderStatus status, Integer totalNumber, Integer orderTotalPrice){

            this.orderId = orderId;
            this.cname = cname;
            this.thumbnailUrl = thumbnailUrl;
            this.status = status;
            this.totalNumber = totalNumber;
            this.orderTotalPrice = orderTotalPrice;
        }
    }
}