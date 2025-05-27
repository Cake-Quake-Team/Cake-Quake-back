//가게 상세 정보 조회 -Response
package com.cakequake.cakequakeback.shop.dto;

import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@Builder(toBuilder = true)

public class ShopDetailResponseDTO {
    private Long shopId;
    private Long uid;
    private String businessNumber;
    private String shopName;
    private String address;
    private String phone;
    private String content;
    private BigDecimal rating;
    private Integer reviewCount;
    private String operationHours;
    private String closeDays;
    private String websiteUrl;
    private String instagramUrl;
    private ShopStatus status;
    private BigDecimal lat;
    private BigDecimal lng;
    private ShopNoticePreviewDTO noticePreview;
    private InfiniteScrollResponseDTO<CakeListDTO> cakes;

    public ShopDetailResponseDTO(Long shopId, Long uid, String businessNumber,
                                 String shopName, String address, String phone,
                                 String content, BigDecimal rating, Integer reviewCount,
                                 String operationHours, String closeDays, String websiteUrl,
                                 String instagramUrl, ShopStatus status, BigDecimal lat, BigDecimal lng,
                                 ShopNoticePreviewDTO noticePreview, InfiniteScrollResponseDTO<CakeListDTO> cakes) {
        this.shopId = shopId;
        this.uid = uid;
        this.businessNumber = businessNumber;
        this.shopName = shopName;
        this.address = address;
        this.phone = phone;
        this.content = content;
        this.rating = rating;
        this.reviewCount = reviewCount;
        this.operationHours = operationHours;
        this.closeDays = closeDays;
        this.websiteUrl = websiteUrl;
        this.instagramUrl = instagramUrl;
        this.status = status;
        this.lat = lat;
        this.lng = lng;
        this.noticePreview = noticePreview;
        this.cakes = cakes;
    }
}
