package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.dto.MappingRequestDTO;
import com.cakequake.cakequakeback.cake.item.dto.MappingResponseDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;

import java.util.List;

public interface MappingService {

    // 매핑 옵션 저장
    MappingResponseDTO saveCakeOptionMapping(MappingRequestDTO requestDTO, CakeItem cakeItem, Long cakeId, Long shopId);

    // 매핑 옵션 목록 조회
    List<CakeOptionMapping> getMappings(Long cakeId);

    // 매핑 옵션 수정
    void updateCakeOptionMappings(CakeItem cakeItem, List<Long> updatedOptionItemIds);
}
