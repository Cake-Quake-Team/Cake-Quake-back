package com.cakequake.cakequakeback.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class shopImageDTO {
    private Long shopImageId;
    private String shopImageUrl;
    private Boolean isThumbnail;

}
