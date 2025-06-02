package com.cakequake.cakequakeback.cake.validator;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.AddCakeDTO;
import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.cake.item.dto.UpdateCakeDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cake.option.repo.OptionItemRepository;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CakeValidator {

    private final ShopRepository shopRepository;
    private final OptionItemRepository optionItemRepository;
    private final MemberRepository memberRepository;
    private final CakeItemRepository cakeItemRepository;

    // shopId 유효성 검사 (DB 접근)
    public Shop validateShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));
    }

    // member 유효성 검사 (DB 접근)
    public Member validateMember(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSING_JWT));
    }

    // optionItem 유효성 검사 (DB 접근)
    public List<OptionItem> validateOptionItems(List<Long> optionItemIds) {
        List<OptionItem> items = optionItemRepository.findAllActiveOptionItem(optionItemIds);

        if (items.size() != optionItemIds.size()) {
            throw new BusinessException(ErrorCode.INVALID_OPTION_ITEM);
        }
        return items;
    }

    // cakeId 유효성 검사 (DB 접근)
    public CakeItem validateCake(Long cakeId) {
        return cakeItemRepository.findById(cakeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRODUCT_ID));
    }

    // 썸네일 유효성 검사
    public String validateThumbnailImageUrl(List<ImageDTO> imageDTOS) {
        return imageDTOS.stream()
                .filter(ImageDTO::getIsThumbnail)  // 썸네일로 지정된 이미지만 필터
                .findFirst()                    // 하나라도 있으면 반환
                .map(ImageDTO::getImageUrl)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_THUMBNAIL_COUNT));
    }

    // 케이크 등록시 유효성 검사
    public void validateAddCake(AddCakeDTO addCakeDTO) {

        if (addCakeDTO.getCname() == null || addCakeDTO.getCname().trim().isEmpty() || addCakeDTO.getCname().length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_LONG_NAME);
        }

        if (addCakeDTO.getPrice() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }

        if (addCakeDTO.getCategory() == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY);
        }

        String description = addCakeDTO.getDescription();

        if (description != null) {
            String trimmed = description.trim();
            if (trimmed.isEmpty() || trimmed.length() > 1000) {
                throw new BusinessException(ErrorCode.MISSING_LONG_DESCRIPTION);
            }
        }

        List<ImageDTO> imageUrls = addCakeDTO.getImageUrls();
        if (imageUrls == null || imageUrls.isEmpty()) {
            throw new BusinessException(ErrorCode.MISSING_IMAGE_LIST);
        }

        List<ImageDTO> thumbnails = imageUrls.stream()
                .filter(ImageDTO::getIsThumbnail)
                .toList();

        if (thumbnails.size() != 1) {
            throw new BusinessException(ErrorCode.INVALID_THUMBNAIL_COUNT);
        }
    }

    // 전체 상품 목록 조회시 유효성 검사
    public void validatePaging(PageRequestDTO pageRequestDTO, CakeCategory category) {

        if (pageRequestDTO.getPage() < 1 || pageRequestDTO.getSize() < 1) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }

        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY);
        }

    }

    // 상품 수정시 유효성 검사
    public void validateUpdateCake(UpdateCakeDTO updateCakeDTO) {

        if (updateCakeDTO.getCname() != null) {
            if(updateCakeDTO.getCname().trim().isEmpty() || updateCakeDTO.getCname().length() > 20) {
                throw new BusinessException(ErrorCode.INVALID_LONG_NAME);
            }
        }

        if (updateCakeDTO.getPrice() != null && updateCakeDTO.getPrice() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }

        if (updateCakeDTO.getDescription() != null) {
            if (updateCakeDTO.getDescription().trim().isEmpty() || updateCakeDTO.getDescription().trim().length() > 1000) {
                throw new BusinessException(ErrorCode.MISSING_LONG_DESCRIPTION);
            }
        }
    }
}
