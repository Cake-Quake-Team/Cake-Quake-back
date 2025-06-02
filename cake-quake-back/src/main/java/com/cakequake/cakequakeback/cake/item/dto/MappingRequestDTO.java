package com.cakequake.cakequakeback.cake.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
// 상품에 옵션 값 연결 요청 DTO
public class MappingRequestDTO {
    private List<Long> optionItemIds;
}
