package com.cakequake.cakequakeback.cart.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 기존 장바구니 상품을 수정(수량/옵션 변경)할 때 사용하는 요청 DTO */
//어차피 상품 항목 추가나 옵션 추가는 상품에서 부르고 장바구니에는 수량만 변경하게 함

public class UpdateCartItem {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request{

        @NotNull(message = "cartItemId는 필수입니다.")
        private Long cartItemId;

        @NotNull(message = "productCnt는 필수입니다.")
        @Min(value = 1, message = "최소 1개 이상이어야 합니다.")
        @Max(value = 99, message = "최대 99개 이하이어야 합니다.")
        private Integer productCnt;
    }
    @Getter
    @Builder
    public static class Response {
        private Long cartItemId;
        private Integer updatedProductCnt;
        private Long updatedItemTotalPrice;
        private String message;
    }
}