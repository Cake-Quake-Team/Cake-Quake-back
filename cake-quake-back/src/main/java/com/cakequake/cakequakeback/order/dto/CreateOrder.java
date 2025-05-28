package com.cakequake.cakequakeback.order.dto;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cart.entities.CartItem;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public class CreateOrder extends BaseEntity {

    @Getter
    @NoArgsConstructor
    public static class Request {

        @NotEmpty(message = "주문하실 때 상품 1개 이상 있으셔야합니다.")
        private List<CakeItem> cakeIds;

        //픽업 날짜
        @NotNull(message = "pickupDate는 필수입니다.")
        private LocalDate pickupDate;

        //픽업 시간
        @NotNull(message = "pickupTime은 필수입니다.")
        private LocalTime pickupTime;

        //사용자 주문 때 요청사항
        private String orderNote;
    }

    @Getter
    @Builder
    public static class Response {

        //주문이 들어간 주문 PK
        private Long orderId;
        //주문 번호
        private String orderNumber;
        //주문 총액
        private Integer totalPrice;
        //예약한 픽업 날짜
        private LocalDate pickupDate;
        //예약한 픽업 시간
        private LocalTime pickupTime;
        //주문 요청사항
        private String orderNote;

        private Response(Long orderId, String orderNumber, Integer totalPrice, LocalDate pickupDate, LocalTime pickupTime, String orderNote) {

            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.totalPrice = totalPrice;

        }
    }
}