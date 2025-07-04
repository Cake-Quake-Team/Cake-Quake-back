package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.item.entities.CakeCategory;
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
    private String shopName;
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
        // isThumbnail == true 인 이미지를 찾아서 thumbnailImageUrl 로 설정합니다.
        String actualThumbnailUrl = imageUrls.stream()
                .filter(image -> image.getIsThumbnail() != null && image.getIsThumbnail())
                .map(ImageDTO::getImageUrl)
                .findFirst()
                .orElse(null);

        if (actualThumbnailUrl == null && cakeItem.getThumbnailImageUrl() != null) {
            actualThumbnailUrl = cakeItem.getThumbnailImageUrl();
        } else if (actualThumbnailUrl == null && !imageUrls.isEmpty()) {
            actualThumbnailUrl = imageUrls.get(0).getImageUrl();
        }

        return CakeDetailDTO.builder()
                .shopName(cakeItem.getShop().getShopName())
                .shopId(cakeItem.getShop().getShopId())
                .cakeId(cakeItem.getCakeId())
                .cname(cakeItem.getCname())
                .description(cakeItem.getDescription())
                .price(cakeItem.getPrice())
                .category(cakeItem.getCategory())
                .thumbnailImageUrl(actualThumbnailUrl)
                .imageUrls(imageUrls)
                .viewCount(cakeItem.getViewCount())
                .orderCount(cakeItem.getOrderCount())
                .isOnsale(cakeItem.getIsOnsale())
                .isDeleted(cakeItem.getIsDeleted())
                .regDate(cakeItem.getRegDate())
                .modDate(cakeItem.getModDate())
                .build();
    }

}
