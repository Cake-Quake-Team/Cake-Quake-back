package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.option.dto.CakeOptionTypeDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
// 상품에 연결된 옵션 타입 조회 DTO
public class MappingResponseDTO {
    private Long cakeId;
    private List<CakeOptionTypeDTO> optionTypes;
}
