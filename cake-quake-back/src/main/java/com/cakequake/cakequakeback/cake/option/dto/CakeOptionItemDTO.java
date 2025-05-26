package com.cakequake.cakequakeback.cake.option.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
// 상품 옵션 값 목록 조회 DTO
public class CakeOptionItemDTO {
    private Long optionItemId;
    private String optionName;
    private int price;
}
