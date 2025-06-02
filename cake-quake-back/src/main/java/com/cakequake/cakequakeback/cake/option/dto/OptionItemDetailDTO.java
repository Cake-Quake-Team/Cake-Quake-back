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
// 케이크 옵션 값 상세 조회
public class OptionItemDetailDTO {
    private Long optionItemId;
    private String optionName;
    private int price;
    private Boolean isDeleted;
    private Long createdBy;
    private Long modifiedBy;
    private LocalDateTime regDate;
    private LocalDateTime modDate;
}
