package com.cakequake.cakequakeback.order.dto.seller;

import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.shop.entities.Shop;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** 판매자 주문 받은 상품 상세 */
// 여기에 결제 관련된 거, 포인트 사용, 쿠폰 사용된 걸 확인하는 걸 넣어야겠지??

public class SellerOrderDetail {

    @Getter
    @Builder
    private static class Response{
        private Long orderId;
        private String orderNumber;
        private OrderStatus status;
        private LocalDate pickupDate;
        private LocalTime pickupTime;
        private List<OrderItem> cake;
        private List<BuyerInfo> buyer;
        private Integer orderTotalPrice;

        private Response(Long orderId, String orderNumber, OrderStatus status, LocalDate pickupDate,
                         LocalTime pickupTime, List<OrderItem> cake, List<BuyerInfo> buyer, Integer orderTotalPrice){

            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.status = status;
            this.pickupDate = pickupDate;
            this.pickupTime = pickupTime;
            this.cake = cake;
            this.buyer = buyer;
            this.orderTotalPrice = orderTotalPrice;
        }
    }

    @Getter
    @Builder
    private static class OrderItem{
        private String cname;
        private Integer productCnt;
        private Integer price;
        private String thumbnailUrl;
        private List<OptionInfo> option;

        private OrderItem(String cname, Integer productCnt, Integer price, String thumbnailImageUrl, List<OptionInfo> option) {
            this.cname = cname;
            this.productCnt = productCnt;
            this.price = price;
            this.thumbnailUrl = thumbnailImageUrl;
            this.option = option;
        }
    }

    @Getter
    @Builder
    private static class BuyerInfo{
        private String uname;
        private String phoneNumber;

        private BuyerInfo(String uname, String phoneNumber) {
            this.uname = uname;
            this.phoneNumber = phoneNumber;
        }
    }

    // 내일 다시 해야할 거
    @Getter
    @Builder
    private static class OptionInfo{
        private Shop shop;
        private Long optionTypeId;
        private String optionType;

        private OptionInfo(Shop shop, Long optionTypeId, String optionType) {
            this.shop = shop;
            this.optionTypeId = optionTypeId;
            this.optionType = optionType;
        }

    }


}