package com.cakequake.cakequakeback.order.dto.seller;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class OrderStatusUpdate {
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonDeserialize(builder = OrderStatusUpdate.Request.RequestBuilder.class)
    public static class Request {

        /** 변경할 주문 상태 (예: "제작중", "제작 완료", "픽업 예정", "픽업 완료" 등) */
        @NotBlank(message = "status는 필수입니다.")
        private String status;

        @JsonPOJOBuilder(withPrefix = "")
        public static class RequestBuilder { }
    }
}