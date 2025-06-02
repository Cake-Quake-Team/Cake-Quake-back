package com.cakequake.cakequakeback.cake.option.dto;

import jakarta.validation.constraints.Min;
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
    private String optionName;
    private Integer price;
    private Boolean isDeleted;      // 삭제 여부
}
