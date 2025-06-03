package com.cakequake.cakequakeback.order.dto.buyer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class CreateOrder {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonDeserialize(builder = CreateOrder.Request.RequestBuilder.class)
    public static class Request{
        //cart로 통해서 주문
        @NotNull(message = "cartItemIds는 필수입니다.")
        @NotEmpty(message = "최소 1개 이상의 CartItem ID가 필요합니다.")
        private List<@Min(value = 1, message = "유효한 CartItem ID를 입력하시요")Long> cartItemIds;

        //직접 주문
        @NotNull(message = "")
        @NotEmpty(message = "최소 1개 이상의 DirectItem이 필요합니다.")
        private List<DirectItem> directItems;

        /** 픽업 날짜 (예: "2025-08-23") */
        @NotNull(message = "pickupDate는 필수입니다.")
        private LocalDate pickupDate;

        /** 픽업 시간 (예: "14:00") */
        @NotNull(message = "pickupTime은 필수입니다.")
        private LocalTime pickupTime;

        /** 주문 시 요청사항 (선택) */
        private String orderNote;

        @JsonPOJOBuilder(withPrefix = "")
        public static class CreateOrderRequestBuilder { }

    }
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonDeserialize(builder = DirectItem.DirectItemBuilder.class)
    public static class DirectItem {
        @NotNull(message = "productId는 필수입니다.")
        @Min(value = 1, message = "유효한 productId를 입력하세요.")
        private Long productId;

        @NotNull(message = "quantity는 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;

        /**
         * 커스텀 옵션이 있을 경우 key-value 형태(JSON으로 넘어옴)
         * 예: {"size":"2호","design":"심플","lettering":"Happy"} 등
         */
        private Map<String, String> options;

        @JsonPOJOBuilder(withPrefix = "")
        public static class DirectItemBuilder { }
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {

        private Long orderId;
        private String orderNumber;
        private Integer orderTotalPrice;
        private LocalDate pickupDate;
        private LocalTime pickupTime;
    }
}