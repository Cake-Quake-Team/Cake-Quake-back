package com.cakequake.cakequakeback.cart.dto;

import java.util.List;

import com.cakequake.cakequakeback.cart.entities.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class GetCart {

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Response {
        private List<ItemInfo> Items;
        private Long cartTotalPrice;


    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemInfo {
        private Long cartItemId;
        private Long cakeId;
        private String cname;
        private int price;
        private String thumbnailImageUrl;
        private Integer productCnt;
        private Long itemTotalPrice;

    }

}
