package com.cakequake.cakequakeback.cart.dto;

import java.util.List;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.cart.entities.CartItem;
import lombok.Builder;
import lombok.Getter;

public class GetCart {

    @Getter
    @Builder
    private static class Response {
        private List<CartItem> cartItems;
        private Long cartTotalPrice;

        private Response(List<CartItem> cakeItems, Long cartToTalPrice) {
            this.cartItems = cakeItems;
            this.cartTotalPrice = cartToTalPrice;

        }

        @Getter
        @Builder
        private static class CartItem {
            private Long cakeId;
            private String cname;
            private int price;
            private String thumbnailImageUrl;
            private Integer productCnt;

            private CartItem(Long cakeId, String cname, int price, String thumbnailImageUrl, Integer productCnt) {
                this.cakeId = cakeId;
                this.cname = cname;
                this.price = price;
                this.thumbnailImageUrl = thumbnailImageUrl;
                this.productCnt = productCnt;
            }

        }

    }
}