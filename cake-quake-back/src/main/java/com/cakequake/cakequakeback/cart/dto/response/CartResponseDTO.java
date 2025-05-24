package com.cakequake.cakequakeback.cart.dto.response;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

/** 클라이언트에 반환할 장바구니 전체 정보 DTO */

@Getter
public class CartResponseDTO {
    private Long cartId;
    private Long userId;
    private Long shopId;
    private Long cakeId;
    private Integer productCnt;
//    private List<CartItemDTO> items;   // 추가로 담긴 옵션별 아이템이 있다면

    @Builder
    public CartResponseDTO(Long cartId, Long userId, Long shopId, Long cakeId,
                           Integer productCnt, List<CartItemDTO> items) {
        this.cartId = cartId;
        this.userId = userId;
        this.shopId = shopId;
        this.cakeId = cakeId;
        this.productCnt = productCnt;
//        this.items = items;
    }
}
