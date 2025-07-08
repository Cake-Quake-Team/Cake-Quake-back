//가게 요약 정보
package com.cakequake.cakequakeback.shop.dto;

import lombok.*;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter

public class ShopPreviewDTO {
    private Long shopId;
    private String shopName;
    private String address;
    private BigDecimal rating;
    private String thumbnailUrl;
    private Long sellerUid;


    public ShopPreviewDTO(Long shopId, String shopName, String address) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.address = address;
    }
}
