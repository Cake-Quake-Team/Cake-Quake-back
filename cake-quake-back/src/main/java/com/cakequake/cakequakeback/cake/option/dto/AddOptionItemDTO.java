package com.cakequake.cakequakeback.cake.option.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 상품 옵션 값 등록 DTO
// 원, 하트, 1호, 초코크림 등등
public class AddOptionItemDTO {
    private Long optionTypeId;
    private String optionName;
    private Integer price;
}
