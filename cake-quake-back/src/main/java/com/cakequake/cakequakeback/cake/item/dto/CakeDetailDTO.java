package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor
// 케이크  상품 상세 조회 응답 DTO
public class CakeDetailDTO {
    private Long shopId;
    private Long cakeId;
    private String cname;
    private String description;
    private int price;
    private CakeCategory category;
    private String thumbnailImageUrl;
    private List<String> imageUrls;
    private int viewCount;
    private int orderCount;
    private Boolean isOnsale;
    private Boolean isDeleted;

    public CakeDetailDTO(Long shopId) {
        this.shopId = shopId;
    }
}
