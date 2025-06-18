package com.cakequake.cakequakeback.shop;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO;
import com.cakequake.cakequakeback.shop.dto.ShopUpdateDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import com.cakequake.cakequakeback.shop.repo.ShopNoticeRepository;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor

public class ShopValidator {

    private final ShopRepository shopRepository;
    private final ShopNoticeRepository shopNoticeRepository;

    public Shop validateShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));
    }

    public ShopNoticeDetailDTO validateNotice(Long noticeId){
        return shopNoticeRepository.findNoticeDetailById(noticeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOTICE_NOT_FOUND));
    }

    public ShopNotice validateShopNotice(Long shopId, Long noticeId){
        return shopNoticeRepository.findByShopNoticeIdAndShopShopId(noticeId, shopId)
                .orElseThrow(()-> new BusinessException(ErrorCode.SHOPNOTICE_NOT_FOUND));
    }

    public void validateUpdateShop(ShopUpdateDTO shopUpdateDTO) {

        String address = shopUpdateDTO.getAddress();
        String content = shopUpdateDTO.getContent();
        LocalTime opentime =shopUpdateDTO.getOpenTime();
        LocalTime closetime =shopUpdateDTO.getCloseTime();


        if (address == null || address.trim().isEmpty() || address.length() > 1000) {
            throw new BusinessException(ErrorCode.INVALID_SHOP_ADDRESS);
        }

        if (content == null || content.trim().isEmpty() || content.length() > 1000) {
            throw new BusinessException(ErrorCode.MISSING_LONG_DESCRIPTION);
        }

        if (opentime == null ||closetime == null || closetime.isBefore(opentime)) {
            throw new BusinessException(ErrorCode.INVALID_BUSINESS_HOURS);
        }



    }



}
