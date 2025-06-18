package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.dto.*;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ShopService {
    //매장 상세 조회
    ShopDetailResponseDTO getShopDetail(Long shopId);

    //매장 목록 조회
    InfiniteScrollResponseDTO<ShopPreviewDTO> getShops(
            int page,int size,ShopStatus status,
            String keyword,String filter, String sort);

    //공지사항 목록 조회
    InfiniteScrollResponseDTO<ShopNoticeDetailDTO> getNoticeList(Long shopId, PageRequestDTO pageRequestDTO);

    //공지사항 상세 조회
    ShopNoticeDetailDTO getNoticeDetail(Long noticeId);

    //공지사항 추가
    Long createNotice(Long shopId, ShopNoticeDTO noticeDTO);

    //공지사항 수정
    void updateNotice(Long shopId, Long noticeId, ShopNoticeDTO noticeDTO);

    //공지사항 삭제
    void deleteNotice(Long shopId, Long noticeId);

    //매장 정보 수정
    void updateShop(Long shopId, ShopUpdateDTO updateDTO, List<MultipartFile> files);
}
