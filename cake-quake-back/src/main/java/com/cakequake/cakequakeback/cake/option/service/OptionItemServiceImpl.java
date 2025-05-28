package com.cakequake.cakequakeback.cake.option.service;

import com.cakequake.cakequakeback.cake.option.dto.*;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import com.cakequake.cakequakeback.cake.option.repo.OptionItemRepository;
import com.cakequake.cakequakeback.cake.option.repo.OptionTypeRepository;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
@ToString
public class OptionItemServiceImpl implements OptionItemService {

    private final OptionItemRepository optionItemRepository;
    private final ShopRepository shopRepository;
    private final OptionTypeRepository optionTypeRepository;

    // shopId가 존재하지 않을 경우
    public void shopExists(Long shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID);
        }
    }

    // optionItemId가 존재하지 않을 경우
    private OptionItem getOptionItemOrThrow(Long optionItemId) {
        return optionItemRepository.findById(optionItemId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID));
    }


    @Override
    // 옵션 값 등록
    public Long addOptionItem(Long shopId, AddOptionItemDTO addOptionItemDTO) {

        shopExists(shopId);

        OptionType optionType = optionTypeRepository.findById(addOptionItemDTO.getOptionTypeId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID));

        OptionItem optionItem = OptionItem.builder()
                .optionType(optionType)
                .optionName(addOptionItemDTO.getOptionName())
                .price(addOptionItemDTO.getPrice())
                .allowQuantity(addOptionItemDTO.getAllowQuantity())
                .position(addOptionItemDTO.getPosition())
                .build();

        optionItemRepository.save(optionItem);

        log.info("옵션 값이 등록되었습니다. 옵션명: " + optionItem.getOptionName());

        return optionItem.getOptionItemId();
    }

    @Override
    @Transactional(readOnly = true)
    // 옵션 값 목록 조회
    public InfiniteScrollResponseDTO<CakeOptionItemDTO> getOptionItemList(Long shopId, PageRequestDTO pageRequestDTO) {

        shopExists(shopId);

        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<CakeOptionItemDTO> itemListPage = optionItemRepository.findOptionItem(shopId, pageable);

        return InfiniteScrollResponseDTO.<CakeOptionItemDTO>builder()
                .content(itemListPage.getContent())
                .hasNext(itemListPage.hasNext())
                .totalCount((int) itemListPage.getTotalElements())
                .build();
    }

    @Override
    // 옵션 값 상세 조회
    public OptionItemDetailDTO getOptionItemDetail(Long shopId, Long optionItemId) {

        shopExists(shopId);

        OptionItem optionItem = getOptionItemOrThrow(optionItemId);

        return OptionItemDetailDTO.builder()
                .optionItemId(optionItem.getOptionItemId())
                .optionName(optionItem.getOptionName())
                .price(optionItem.getPrice())
                .price(optionItem.getPrice())
                .isUsed(optionItem.getIsUsed())
                .allowQuantity(optionItem.getAllowQuantity())
                .position(optionItem.getPosition())
                .build();
    }

    @Override
    // 옵션 값 수정
    public void updateOptionItem(Long shopId, Long optionItemId, UpdateOptionItemDTO updateOptionItemDTO) {

        shopExists(shopId);

        OptionItem optionItem = getOptionItemOrThrow(optionItemId);

        optionItem.updateFromDTO(updateOptionItemDTO);
    }

    @Override
    // 옵션 값 삭제
    public void deleteOptionItem(Long shopId, Long optionItemId) {

        shopExists(shopId);

        OptionItem optionItem = getOptionItemOrThrow(optionItemId);

        optionItemRepository.delete(optionItem);
    }
}
