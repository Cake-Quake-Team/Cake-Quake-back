//가게 요약 정보
package com.cakequake.cakequakeback.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor

public class ShopPreviewDTO {
    private Long shopId;
    private String shopName;
    private String address;
    private BigDecimal rating;
    private String thumbnailUrl;

}
