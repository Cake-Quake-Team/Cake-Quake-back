package com.cakequake.cakequakeback.cake.option.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 상품 옵션 타입 조회 DTO
public class CakeOptionTypeDTO {
    private Long optionTypeId;
    private String optionType;
}
