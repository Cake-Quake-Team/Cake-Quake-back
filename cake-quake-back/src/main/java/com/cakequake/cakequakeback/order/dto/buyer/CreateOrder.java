package com.cakequake.cakequakeback.order.dto.buyer;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
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

        @NotNull(message = "shopId는 필수입니다.")
        private Long shopId;

        //cart로 통해서 주문
        private List<@Min(value = 1, message = "유효한 CartItem ID를 입력하시요")Long> cartItemIds;

        //직접 주문
        private List<DirectItem> directItems;

        /** 픽업 날짜 (예: "2025-08-23") */
        @NotNull(message = "pickupDate는 필수입니다.")
        private LocalDate pickupDate;

        /** 픽업 시간 (예: "14:00") */
        @NotNull(message = "pickupTime은 필수입니다.")
        private LocalTime pickupTime;

        /** 주문 시 요청사항 (선택) */
        private String orderNote;

        // ⭐포인트 사용 필드 추가
        private Integer usedPoints;

        // 조건 유효성 검사
        @AssertTrue(message = "cartItemIds 또는 directItems 중 하나만 제공되어야 합니다.")
        public boolean isEitherCartOrDirectProvided() {
            boolean cartProvided = cartItemIds != null && !cartItemIds.isEmpty();
            boolean directProvided = directItems != null && !directItems.isEmpty();
            return cartProvided ^ directProvided;
        }


        @JsonPOJOBuilder(withPrefix = "")
        public static class RequestBuilder { }
    }


    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonDeserialize(builder = DirectItem.DirectItemBuilder.class)
    public static class DirectItem {

        @NotNull(message = "shopId는 필수입니다.")
        private Long shopId;

        @NotNull(message = "cakeId는 필수입니다.")
        private Long cakeId;

        @NotNull(message = "productId는 필수입니다.")
        @Min(value = 1, message = "유효한 productId를 입력하세요.")
        private Long cakeItemId;

        @NotNull(message = "quantity는 필수입니다.")
        @Min(value = 1, message = "수량은 1 이상이어야 합니다.")
        private Integer quantity;

        /**
         * 커스텀 옵션이 있을 경우 key-value 형태(JSON으로 넘어옴)
         * 예: {"size":"2호","design":"심플","lettering":"Happy"} 등
         */
        private Map<Long,Integer> options;

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
        private String orderNote;
        private Long shopId;
    }

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SelectedOptionDetail {
        private Long mappingId;
        private String optionName;
        private Integer price;
        private Integer count;
        private String optionType; // ⭐ 이 필드를 추가합니다. ⭐
    }
}