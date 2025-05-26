package com.cakequake.cakequakeback.cake.option.dto;

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
    @NotNull(message = "옵션 타입은 필수입니다.")
    private String optionType;
}
