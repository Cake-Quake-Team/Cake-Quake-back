package com.cakequake.cakequakeback.shop.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class ImageResponseDTO {
    private List<ShopImageDTO> shopImageDTOS;
    private String thumbnailUrl;

}
