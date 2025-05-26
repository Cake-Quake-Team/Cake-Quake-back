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
// 옵션 타입 수정 DTO
public class UpdateOptionTypeDTO {
    @NotNull(message = "옵션 타입 Id는 필수입니다.")
    private Long optionTypeId;

    @NotNull(message = "옵션 타입은 필수입니다.")
    private String optionType;

    private Boolean isUsed;         // 사용 여부
    private Boolean isRequired;     // 필수 선택 여부

    private Integer minSelection;   // 최소 선택 개수
    private Integer maxSelection;   // 최대 선택 개수
}
