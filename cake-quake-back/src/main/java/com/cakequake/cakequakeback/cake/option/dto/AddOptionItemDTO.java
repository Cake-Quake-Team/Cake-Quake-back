package com.cakequake.cakequakeback.cake.option.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "옵션 값은 필수입니다.")
    private String optionName;

    @NotNull(message = "가격은 0원보다 커야 합니다.")
    @Min(value = 1, message = "가격은 0원보다 커야 합니다.")
    private Integer price;
}
