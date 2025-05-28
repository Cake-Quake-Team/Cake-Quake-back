package com.cakequake.cakequakeback.order.dto.buyer;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** 구매자 주문 상세 */
// 여기도 결제 관련된 거, 포인트 사용, 쿠폰 사용된 걸 확인하는 걸 넣어야겠지??

public class BuyerOrderDetail {

    @Getter
    @Builder
    @AllArgsConstructor
    public static class Response {
        private Long orderId;
        private String orderNumber;
        private String shopName;
        private Long totalPrice;
        private OrderStatus status;
        private LocalDate pickupDate;
        private LocalTime pickupTime;
        private List<CakeItem> cakeItems;

        private Response(Long orderId, String orderNumber, String shopName, Long totalPrice,
                         OrderStatus status, LocalDate pickupDate, LocalTime pickupTime) {
            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.shopName = shopName;
            this.totalPrice = totalPrice;
            this.status = status;
            this.pickupDate = pickupDate;
            this.pickupTime = pickupTime;
        }
    }

    @Getter
    @Builder
    public static class Item {
        private Long cakeId;
        private String cname;
        private Long price;
        private String thumbnailImageUrl;

        private Item(Long cakeId, String cname, Long price, String thumbnailImageUrl) {
            this.cakeId = cakeId;
            this.cname = cname;
            this.price = price;
            this.thumbnailImageUrl = thumbnailImageUrl;
        }
    }
}