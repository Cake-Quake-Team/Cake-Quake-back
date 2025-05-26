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
// 옵션 값 수정 DTO
public class UpdateOptionItemDTO {
    @NotNull(message = "옵션 값 Id는 필수입니다.")
    private Long optionItemId;

    @NotNull(message = "옵션 값은 필수입니다.")
    private String optionName;

    @NotNull(message = "가격은 0원보다 커야 합니다.")
    @Min(value = 1, message = "가격은 0원보다 커야 합니다.")
    private Integer price;

    private Boolean isUsed;         // 사용 여부
    private Integer position;       // 표시 순서
    private Boolean allowQuantity;  // 수량 조절 가능 여부
}
