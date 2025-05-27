package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticePreviewDTO;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.repo.ShopNoticeRepository;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor

public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;
    private final ShopNoticeRepository shopNoticeRepository;

    @Override
    public ShopDetailResponseDTO getShopDetail(Long shopId) {
        // 1. 기본 매장 정보 DTO 조회
        ShopDetailResponseDTO dto = shopRepository.selectDTO(shopId)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        // 2. 가장 최근 공지사항 조회
        Optional<ShopNotice> optionalNotice = shopNoticeRepository
                .findLatestByShopId(shopId, PageRequest.of(0, 1))
                .stream().findFirst();

        // 3. 공지사항이 있을 경우 미리보기 생성 및 DTO에 세팅
        optionalNotice.ifPresent(notice -> {
            String fullContent = notice.getContent();
            String previewContent = fullContent.length() <= 30 ? fullContent : fullContent.substring(0, 30) + "...";

            ShopNoticePreviewDTO previewDTO = new ShopNoticePreviewDTO(
                    notice.getShopNoticeId(),
                    shopId,
                    notice.getTitle(),
                    previewContent,
                    notice.getRegDate(),
                    notice.getModDate()
            );

            dto.setNoticePreview(previewDTO);
        });

        return dto;
    }
    //매장 목록 조회
    @Override
    public InfiniteScrollResponseDTO<ShopPreviewDTO> getShopsByStatus(PageRequestDTO pageRequestDTO, ShopStatus status) {
        Pageable pageable = pageRequestDTO.getPageable("shopId"); // 정렬 기준은 필요에 따라 변경

        Page<ShopPreviewDTO> page = shopRepository.findAll(status, pageable);

        return InfiniteScrollResponseDTO.<ShopPreviewDTO>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements())
                .build();
    }





}




