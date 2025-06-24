package com.cakequake.cakequakeback.schedule.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter

public class ShopScheduleDTO {
    private Long shopId;
    private String shopName;
    private String address;
    private BigDecimal rating;
    private String thumbnailUrl;
    private String openTime;
    private String closeTime;
    private String closeDays;

    public ShopScheduleDTO(
            Long shopId,
            String shopName,
            String address,
            BigDecimal rating,
            String thumbnailImageUrl,
            LocalTime openTimeFromEntity,
            LocalTime closeTimeFromEntity,
            String closeDays) {
        this.shopId = shopId;
        this.shopName = shopName;
        this.address = address;
        this.rating = rating;
        this.thumbnailUrl = thumbnailImageUrl;
        this.openTime = (openTimeFromEntity != null) ? openTimeFromEntity.toString() : null;
        this.closeTime = (closeTimeFromEntity != null) ? closeTimeFromEntity.toString() : null;
        this.closeDays = closeDays;
    }


}


