package com.cakequake.cakequakeback.order.dto.buyer;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/** 구매자 주문 목록 */
public class BuyerOrderList {

    @Getter
    @Builder
    public static class Response{
        private Long orderId;
        private String orderNumber;
        private String shopName;
        private Integer orderTotalPrice;
        private String status;
        private String orderType;
        private LocalDate pickupDate;
        private LocalTime pickupTime;

        private Response(Long orderId, String orderNumber, String shopName, Integer orderTotalPrice,
                         String status, String orderType, LocalDate pickupDate, LocalTime pickupTime){

            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.shopName = shopName;
            this.orderTotalPrice = orderTotalPrice;
            this.status = status;
            this.orderType = orderType;
            this.pickupDate = pickupDate;
            this.pickupTime = pickupTime;
        }
    }

}
