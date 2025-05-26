package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticePreviewDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import com.cakequake.cakequakeback.shop.repo.ShopNoticeRepository;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
        Optional<ShopNotice> optionalNotice = shopNoticeRepository.findLatestByShopId(shopId);

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
}




