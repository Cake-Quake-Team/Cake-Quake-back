package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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
    private List<ImageDTO> imageUrls;
    private int viewCount;
    private int orderCount;
    private Boolean isOnsale;
    private Boolean isDeleted;
    private LocalDateTime regDate;
    private LocalDateTime modDate;

    public static CakeDetailDTO from(CakeItem cakeItem, List<ImageDTO> imageUrls) {
        return CakeDetailDTO.builder()
                .shopId(cakeItem.getShop().getShopId())
                .cakeId(cakeItem.getCakeId())
                .cname(cakeItem.getCname())
                .description(cakeItem.getDescription())
                .price(cakeItem.getPrice())
                .category(cakeItem.getCategory())
                .thumbnailImageUrl(cakeItem.getThumbnailImageUrl())
                .viewCount(cakeItem.getViewCount())
                .orderCount(cakeItem.getOrderCount())
                .isOnsale(cakeItem.getIsOnsale())
                .isDeleted(cakeItem.getIsDeleted())
                .imageUrls(imageUrls)
                .regDate(cakeItem.getRegDate())
                .modDate(cakeItem.getModDate())
                .build();
    }
}
