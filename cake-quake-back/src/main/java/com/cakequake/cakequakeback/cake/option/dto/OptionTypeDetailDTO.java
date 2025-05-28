package com.cakequake.cakequakeback.cake.option.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 케이크 옵션 타입 상세 조회 DTO
public class OptionTypeDetailDTO {
    private Long optionTypeId;
    private String optionType;
    private Boolean isRequired;
    private Boolean isUsed;
    private Integer minSelection;
    private Integer maxSelection;
    private Long createdBy;
    private Long modifiedBy;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
