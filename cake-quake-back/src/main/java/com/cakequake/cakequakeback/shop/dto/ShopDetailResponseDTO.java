//가게 상세 정보 조회 -Response
package com.cakequake.cakequakeback.shop.dto;

import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
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
    private LocalTime openTime;
    private LocalTime closeTime;
    private String closeDays;
    private String websiteUrl;
    private String instagramUrl;
    private String thumbnailUrl;
    private List<shopImageDTO> images;
    private ShopStatus status;
    private BigDecimal lat;
    private BigDecimal lng;
    private ShopNoticePreviewDTO noticePreview;
    private List<CakeListDTO> cakes;

}
