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

        // 1. 주소 (address) 유효성 검사
        // address 필드가 DTO에 포함되어 있고 (null이 아니며),
        // 값이 비어있거나 (trim 후) 길이가 1000자를 초과하는 경우에만 예외 발생
        if (shopUpdateDTO.getAddress() != null) {
            String address = shopUpdateDTO.getAddress();
            if (address.trim().isEmpty() || address.length() > 1000) {
                throw new BusinessException(ErrorCode.INVALID_SHOP_ADDRESS);
            }
        }

        // 2. 상세 설명 (content) 유효성 검사
        // content 필드가 DTO에 포함되어 있고 (null이 아니며),
        // 값이 비어있거나 (trim 후) 길이가 1000자를 초과하는 경우에만 예외 발생
        // content가 항상 필수라면, null 체크는 유지하되 `trim().isEmpty()`는 계속 확인해야 합니다.
        // 현재 코드에 따르면 content는 항상 필수라고 가정하고 그대로 둡니다.
        // 만약 content도 부분 업데이트 대상이고 null일 수 있다면 address와 동일하게 null 체크를 추가하세요.
        String content = shopUpdateDTO.getContent();
        if (content == null || content.trim().isEmpty() || content.length() > 1000) {
            // 이 에러 코드가 "긴 설명이 누락되었습니다(1~1000자)"와 연결된 것이므로,
            // content가 null이거나 비어있거나 너무 길면 이 에러가 발생합니다.
            // 클라이언트에서 content를 항상 보내야 한다면 이 로직은 유지됩니다.
            throw new BusinessException(ErrorCode.MISSING_LONG_DESCRIPTION);
        }


        // 3. 영업 시간 (openTime, closeTime) 유효성 검사
        LocalTime opentime = shopUpdateDTO.getOpenTime();
        LocalTime closetime = shopUpdateDTO.getCloseTime();

        // 두 시간 모두 DTO에 포함되어 있을 때만 유효성 검사 수행
        // 만약 둘 중 하나만 포함된 경우를 허용하지 않는다면, 아래 else if 블록을 사용하여 예외를 던질 수 있습니다.
        if (opentime != null && closetime != null) {
            if (closetime.isBefore(opentime)) {
                throw new BusinessException(ErrorCode.INVALID_BUSINESS_HOURS);
            }
        }
        // else if (opentime != null || closetime != null) {
        //     // 요구사항에 따라 둘 중 하나만 넘어오면 오류로 간주할 경우:
        //     // throw new BusinessException(ErrorCode.INVALID_BUSINESS_HOURS);
        // }
        // 둘 중 하나만 넘어온 경우는 현재 코드에서 아무런 예외도 던지지 않도록 수정되었습니다.
        // 이는 부분 업데이트를 가정할 때 더 유연한 처리 방식입니다.
        // 만약 '영업시간은 항상 함께 업데이트되어야 한다'는 비즈니스 규칙이 있다면,
        // 위 주석 처리된 else if 블록을 활성화하여 적절한 예외를 던져야 합니다.
    }


    }



