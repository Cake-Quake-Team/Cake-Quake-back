//가게 요약 정보
package com.cakequake.cakequakeback.shop.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Builder
public class ShopPreviewDTO {
    private Long shopId;
    private String shopName;
    private String address;
    private BigDecimal rating;

    public ShopPreviewDTO(Long shopId, String shopName, String address, BigDecimal rating) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.address = address;
        this.rating = rating;
    }
}
