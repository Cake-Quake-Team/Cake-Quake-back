package com.cakequake.cakequakeback.order.dto;

import com.cakequake.cakequakeback.cart.entities.CartItem;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class CreateOrder extends BaseEntity {

    @Getter
    @NoArgsConstructor
    public static class Request {
        @NotEmpty(message = "cartItemIds는 하나 이상 필요합니다.")
        private List<Long> cartItemIds;

        @NotEmpty(message = "directItems는 하나 이상 필요합니다.")
        private List<DirectItem> directItems;

        private String orderNote;
    }

    @Getter
    @Builder
    public static class Response {
        private int code;
        private String message;
        private Data data;

        @Getter
        @Builder
        public static class Data {
            private Long orderId;
            private String orderNumber;
            private Long totalPrice;
        }
    }

    @Getter
    @NoArgsConstructor
    public static class DirectItem {
        @NotNull(message = "productId는 필수입니다.")
        private Long productId;
        @NotNull(message = "quantity는 필수입니다.")
        private Integer quantity;
        private Object options;
    }
}

