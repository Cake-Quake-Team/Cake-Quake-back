package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.*;
import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;

import java.util.List;

public interface CakeItemService {

    // 케이크 등록
    MappingResponseDTO addCake(AddCakeDTO addCakeDTO, Long shopId);

    // 케이크 목록 조회
    InfiniteScrollResponseDTO<CakeListDTO> getAllCakeList(PageRequestDTO pageRequestDTO, CakeCategory category);

    // 특정 매장의 케이크 목록 조회
    InfiniteScrollResponseDTO<CakeListDTO> getShopCakeList(Long shopId, PageRequestDTO pageRequestDTO, CakeCategory category);

    // 케이크 상세 조회
    MappingResponseDTO getCakeDetail(Long shopId, Long cakeId);

    // 케이크 수정
    void updateCake(Long shopId, Long cakeId, UpdateCakeDTO updateCakeDTO);

    // 케이크 삭제
    void deleteCake(Long shopId, Long cakeId);

}
