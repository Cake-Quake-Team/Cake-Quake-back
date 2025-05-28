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

        //장바구니에서 상품 주문하기 눌렀을 때
        @NotEmpty(message = " 장바구니에서 주문하실 때 상품 하나 이상 필요합니다.")
        private List<CartItem> cartItemIds;

        // 바로 다이렉트로 주문할 때
        private List<CakeItem> cakeId;

        @NotNull(message = "pickupDate는 필수입니다.")
        private LocalDate pickupDate;

        @NotNull(message = "pickupTime은 필수입니다.")
        private LocalTime pickupTime;

        //사용자 주문 때 요청
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
        //예약한 픽업 일시
        private LocalDate reservedDateTime;

        private Response(Long orderId, String orderNumber, Integer totalPrice, LocalDate reservedDateTime) {

            this.orderId = orderId;
            this.orderNumber = orderNumber;
            this.totalPrice = totalPrice;
            this.reservedDateTime = reservedDateTime;
        }
    }
}