package com.cakequake.cakequakeback.cake.option.service;

import com.cakequake.cakequakeback.cake.option.dto.AddOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.dto.CakeOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.dto.OptionItemDetailDTO;
import com.cakequake.cakequakeback.cake.option.dto.UpdateOptionItemDTO;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;

public interface OptionItemService {

    // 케이크 옵션 값 등록
    // @param shopId 매장 ID
    Long addOptionItem(Long shopId, AddOptionItemDTO addOptionItemDTO);

    // 케이크 옵션 값 목록 조회
    // @param shopId 매장 ID
    InfiniteScrollResponseDTO<CakeOptionItemDTO> getOptionItemList(Long shopId, PageRequestDTO pageRequestDTO);

    // 케이크 옵션 값 상세 조회
    // @param shopId 매장 ID
    // @param optionItemId 옵션 값 ID
    OptionItemDetailDTO getOptionItemDetail(Long shopId, Long optionItemId);

    // 케이크 옵션 값 수정
    // @param shopId 매장 ID
    // @param optionItemId 옵션 값 ID
    void updateOptionItem(Long shopId, Long optionItemId, UpdateOptionItemDTO updateOptionItemDTO);

    // 케이크 옵션 값 삭제
    // @param shopId 매장 ID
    // @param optionItemId 옵션 값 ID
    void deleteOptionItem(Long shopId, Long optionItemId);
}
