package com.cakequake.cakequakeback.cake.validator;

import com.cakequake.cakequakeback.cake.option.dto.AddOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.dto.AddOptionTypeDTO;
import com.cakequake.cakequakeback.cake.option.dto.UpdateOptionTypeDTO;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import com.cakequake.cakequakeback.cake.option.repo.OptionItemRepository;
import com.cakequake.cakequakeback.cake.option.repo.OptionTypeRepository;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OptionValidator {

    private final OptionItemRepository optionItemRepository;
    private final ShopRepository shopRepository;
    private final OptionTypeRepository optionTypeRepository;


    // shopId 유효성 검사 (DB 접근)
    public Shop validateShop(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));
    }

    // optionItemId가 존재하지 않을 경우
    public OptionItem vlidateOptionItem(Long optionItemId) {
        return optionItemRepository.findById(optionItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID));
    }

    // optionTypeId가 존재하지 않는 경우
    public OptionType validateOptionType(Long optionTypeId) {
        return optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID));
    }

    //옵션 타입 등록시 유효성 검사
    public void validateAddOptionType(AddOptionTypeDTO addOptionTypeDTO) {

        if (addOptionTypeDTO.getOptionType() == null || addOptionTypeDTO.getOptionType().trim().isEmpty() || addOptionTypeDTO.getOptionType().length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_TYPE);
        }

        if (addOptionTypeDTO.getIsRequired() != null && addOptionTypeDTO.getIsRequired() == true) {
            if (addOptionTypeDTO.getMinSelection() == 0) {
                throw new BusinessException(ErrorCode.INVALID_SELECTION_WHEN_REQUIRED);
            }
        }

        if (addOptionTypeDTO.getMinSelection() != null && addOptionTypeDTO.getMaxSelection() != null) {
            if (addOptionTypeDTO.getMinSelection() > addOptionTypeDTO.getMaxSelection()) {
                throw new BusinessException(ErrorCode.INVALID_SELECTION_RANGE);
            }
        }
    }

    // 옵션 값 등록시 유효성 검사
    public void validateAddOptionItem(AddOptionItemDTO addOptionItemDTO) {

        if (addOptionItemDTO.getOptionName() == null || addOptionItemDTO.getOptionName().trim().isEmpty() || addOptionItemDTO.getOptionName().length() > 20){
            throw new BusinessException(ErrorCode.INVALID_LONG_NAME);
        }

        if (addOptionItemDTO.getPrice() <= 0) {
            throw new BusinessException(ErrorCode.INVALID_PRICE);
        }
    }

    // 옵션 타입 수정시 유효성 검사
    public void validateUpdateOptionType(UpdateOptionTypeDTO updateOptionTypeDTO) {

        if (updateOptionTypeDTO.getOptionType() != null){
            if(updateOptionTypeDTO.getOptionType().trim().isEmpty() || updateOptionTypeDTO.getOptionType().length() > 20
            ) {
                throw new BusinessException(ErrorCode.INVALID_TYPE);
            }
        }
        if (updateOptionTypeDTO.getIsRequired() != null && updateOptionTypeDTO.getIsRequired()) {
            if (updateOptionTypeDTO.getMinSelection() == 0) {
                throw new BusinessException(ErrorCode.INVALID_SELECTION_WHEN_REQUIRED);
            }
        }
        if (updateOptionTypeDTO.getMinSelection() != null && updateOptionTypeDTO.getMaxSelection() != null) {
            if (updateOptionTypeDTO.getMinSelection() > updateOptionTypeDTO.getMaxSelection()) {
                throw new BusinessException(ErrorCode.INVALID_SELECTION_RANGE);
            }
        }
    }

    // 옵션 목록 조회시 유효성 검사
    public void validatePaging(PageRequestDTO pageRequestDTO) {

        if (pageRequestDTO.getPage() < 1 || pageRequestDTO.getSize() < 1) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }
    }

}
