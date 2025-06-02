package com.cakequake.cakequakeback.cake.option.dto;

import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 상품 옵션 타입 등록 DTO
// 시트모양, 시트 크기, 속크림, 겉크림 등등
public class AddOptionTypeDTO {

    private String optionType;
    private Boolean isRequired = false;
    private Integer minSelection = 0;
    private Integer maxSelection = 1;
}
