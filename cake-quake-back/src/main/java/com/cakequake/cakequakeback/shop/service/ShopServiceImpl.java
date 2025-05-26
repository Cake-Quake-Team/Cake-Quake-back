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

@Service
@Transactional
@RequiredArgsConstructor

public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;
    private final ShopNoticeRepository shopNoticeRepository;

    @Override
    public ShopDetailResponseDTO getShopDetail(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        ShopNoticePreviewDTO noticePreview = shopNoticeRepository
                .findTopByShopIdOrderByCreatedDateDesc(shopId)
                .map(notice -> {
                    String fullContent = notice.getContent();
                    String preview = fullContent.length() <= 30 ? fullContent : fullContent.substring(0, 30) + "...";

                    return new ShopNoticePreviewDTO(
                            notice.getShopnoticeId(),
                            notice.getShop().getShopId(),
                            notice.getTitle(),
                            preview, // Service에서 잘라서 전달
                            notice.getRegDate(),
                            notice.getModDate()
                    );
                })
                .orElse(null);

        return new ShopDetailResponseDTO(
                shop.getShopId(),
                shop.getMember().getUid(),
                shop.getBusinessNumber(),
                shop.getShopName(),
                shop.getAddress(),
                shop.getPhone(),
                shop.getContent(),
                shop.getRating(),
                shop.getReviewCount(),
                shop.getOperationHours(),
                shop.getCloseDays(),
                shop.getWebsiteUrl(),
                shop.getInstagramUrl(),
                shop.getStatus(),
                shop.getLat(),
                shop.getLng(),
                noticePreview
        );
    }
}
