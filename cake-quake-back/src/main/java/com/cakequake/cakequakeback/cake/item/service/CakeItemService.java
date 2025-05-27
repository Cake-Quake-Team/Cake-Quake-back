package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.AddCakeDTO;
import com.cakequake.cakequakeback.cake.item.dto.CakeDetailDTO;
import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.dto.UpdateCakeDTO;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;

import java.util.List;

public interface CakeItemService {

    // 케이크 등록
    Long addCake(AddCakeDTO addCakeDTO, Long shopId, Long uid);

    // 케이크 목록 조회
    InfiniteScrollResponseDTO<CakeListDTO> getAllCakeList(PageRequestDTO pageRequestDTO, CakeCategory category);

    // 특정 매장의 케이크 목록 조회
    InfiniteScrollResponseDTO<CakeListDTO> getShopCakeList(Long shopId, PageRequestDTO pageRequestDTO, CakeCategory category);

    // 케이크 상세 조회
    CakeDetailDTO getCakeDetail(Long cakeId);

    // 케이크 수정
    void updateCake(Long shopId, Long cakeId, UpdateCakeDTO updateCakeDTO);

    // 케이크 삭제
    void deleteCake(Long shopId, Long cakeId);

}
