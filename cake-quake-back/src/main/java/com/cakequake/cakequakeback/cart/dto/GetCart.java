package com.cakequake.cakequakeback.cart.dto;

import java.util.List;

import com.cakequake.cakequakeback.order.dto.buyer.CreateOrder;

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
        private List<ItemInfo> items;
        private Long cartTotalPrice;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ItemInfo {
        private Long cartItemId;
        private Long shopId;
        private Long cakeId;
        private String cname;
        private int price;
        private String thumbnailImageUrl;
        private Integer productCnt;
        private Long itemTotalPrice;
        private List<CreateOrder.SelectedOptionDetail> selectedOptions;

    }

}