package com.cakequake.cakequakeback.cart.dto;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

//장바구니에 새 아이템을 추가할 때 사용하는 요청 DTO
public class AddCart {

    @Getter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class Request {

        //장바구니에 담을 상품
        @Valid
        @NotNull(message = "상품 ID 필수입니다.")
        private  Long cakeItemId;

        //담은 상품 수량(1~99)
        @Min(value = 1, message = "productCnt는 최소 1개 이상이어야 합니다.")
        @Max(value = 99, message="productCnt는 최소 99개 미만이어야 합니다.")
        private Integer productCnt;
    }

    @Getter
    @Builder
    public static class Response {

        private Long cartItemId; // 장바구니에 추가된 아이템의 ID
        private Long cakeItemId;
        private String cname; // (선택적) 상품명
        private Integer productCnt; // 최종 수량
        private Long itemTotalPrice; // 해당 아이템의 총 가격
    }


}
