package com.cakequake.cakequakeback.cart.dto;

import com.cakequake.cakequakeback.cart.entities.CartItem;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/** 클라이언트에 반환할 장바구니 전체 정보 DTO */

public class DeletedCartItem {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    private static class Request {

        //장바구니에 담겨있는 상품들 삭제
        @NotNull(message = "삭제할 cartItemIds는 필수입니다.")
        @Size(min = 1, message = "최소 1개 이상의 상품을 지정해야 합니다.")
        private List<Long> cartItemIds;
    }
    @Getter
    @Builder
    public static class Response {
        private List<Long> deletedCartItemIds; // 성공적으로 삭제된 cartItem ID 목록
        private String message; // 예: "선택한 상품이 장바구니에서 삭제되었습니다."
    }}