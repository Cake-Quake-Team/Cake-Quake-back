package com.cakequake.cakequakeback.cake.option.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 옵션 전체(타입 + 값) 조회 DTO
public class CakeOptionListDTO {
    private Long optionTypeId;
    private String optionType;
    private Boolean isUsed;
    private Boolean isRequired;
    private int minSelection;
    private int maxSelection;
    private List<CakeOptionItemDTO> optionItems;
}
