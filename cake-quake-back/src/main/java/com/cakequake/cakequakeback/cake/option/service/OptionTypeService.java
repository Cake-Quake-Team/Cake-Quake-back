package com.cakequake.cakequakeback.cake.option.service;


import com.cakequake.cakequakeback.cake.option.dto.*;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;

public interface OptionTypeService {

    // 케이크 옵션 타입 등록
    // @param shopId 매장 ID
    Long addOptionType(Long shopId, AddOptionTypeDTO addOptionTypeDTO);

    // 케이크 옵션 타입 목록 조회
    // @param shopId 매장 ID
    InfiniteScrollResponseDTO<CakeOptionTypeDTO> getOptionTypeList(PageRequestDTO pageRequestDTO, Long shopId);

    // 케이크 옵션 타입 상세 조회
    // @param shopId 매장 ID
    // @Param optionTypeId 옵션 타입 ID
    OptionTypeDetailDTO getOptionTypeDetail(Long shopId, Long optionTypeId);

    // 케이크 옵션 타입 수정
    // @param shopId 매장 ID
    // @Param optionTypeId 옵션 타입 ID
    void updateOptionType(Long shopId, Long optionTypeId, UpdateOptionTypeDTO updateOptionTypeDTO);

    // 케이크 옵션 타입 삭제
    // @param shopId 매장 ID
    // @Param optionTypeId 옵션 타입 ID'
    void deleteOptionType(Long shopId, Long optionTypeId);
}
