package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;

public interface ShopService {
    //매장 상세 조회
    ShopDetailResponseDTO getShopDetail(Long shopId);

    //매장 목록 조회
    InfiniteScrollResponseDTO<ShopPreviewDTO> getShopsByStatus(PageRequestDTO pageRequestDTO, ShopStatus status);

    //공지사항 목록 조회
    InfiniteScrollResponseDTO<ShopNoticeDetailDTO> getNoticeList(Long shopId, PageRequestDTO pageRequestDTO);

    //공지사항 상세 조회
    ShopNoticeDetailDTO getNoticeDetail(Long noticeId);
}
