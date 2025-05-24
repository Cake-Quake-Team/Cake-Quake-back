package com.cakequake.cakequakeback.shop.dto;

import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@Builder

public class ShopDetailDTO {
    private Long shopId;
    private Long userId;
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

    public ShopDetailDTO(Long shopId, Long userId, String businessNumber, String shopName, String address,
                         String phone, String content, BigDecimal rating, Integer reviewCount, String operationHours,
                         String closeDays, String websiteUrl, String instagramUrl, ShopStatus status, BigDecimal lat,
                         BigDecimal lng) {
        this.shopId = shopId;
        this.userId = userId;
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
    }
}
