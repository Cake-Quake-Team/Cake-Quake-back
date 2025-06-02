package com.cakequake.cakequakeback.cake.option.service;

import com.cakequake.cakequakeback.cake.option.dto.*;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import com.cakequake.cakequakeback.cake.option.repo.OptionItemRepository;
import com.cakequake.cakequakeback.cake.option.repo.OptionTypeRepository;
import com.cakequake.cakequakeback.cake.validator.OptionValidator;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
@ToString
public class OptionItemServiceImpl implements OptionItemService {

    private final OptionItemRepository optionItemRepository;
    private final ShopRepository shopRepository;
    private final OptionTypeRepository optionTypeRepository;
    private final OptionValidator optionValidator;

    @Override
    // 옵션 값 등록
    public Long addOptionItem(Long shopId, AddOptionItemDTO addOptionItemDTO) {

        optionValidator.validateShop(shopId);
        OptionType optionType = optionValidator.validateOptionType(addOptionItemDTO.getOptionTypeId());
        optionValidator.validateAddOptionItem(addOptionItemDTO);

        // 삭제된 동일 이름의 OptionName이 존재하는지 확인
        Optional<OptionItem> alreadyDeleted = optionItemRepository.findByOptionNameAndIsDeletedTrue(addOptionItemDTO.getOptionName());

        if (alreadyDeleted.isPresent()) {
            OptionItem deleted = alreadyDeleted.get();
            deleted.restoreOptionItem(addOptionItemDTO);

            return deleted.getOptionItemId();
        }

        OptionItem optionItem = OptionItem.builder()
                .optionType(optionType)
                .optionName(addOptionItemDTO.getOptionName())
                .price(addOptionItemDTO.getPrice())
                .version(1)
                .isDeleted(false)
                .build();

        optionItemRepository.save(optionItem);

        log.info("옵션 값이 등록되었습니다. 옵션명: " + optionItem.getOptionName());

        return optionItem.getOptionItemId();
    }

    @Override
    @Transactional(readOnly = true)
    // 옵션 값 목록 조회
    public InfiniteScrollResponseDTO<CakeOptionItemDTO> getOptionItemList(Long shopId, PageRequestDTO pageRequestDTO) {

        optionValidator.validateShop(shopId);
        optionValidator.validatePaging(pageRequestDTO);

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

        optionValidator.validateShop(shopId);
        OptionItem optionItem = optionValidator.vlidateOptionItem(optionItemId);

        return OptionItemDetailDTO.builder()
                .optionItemId(optionItem.getOptionItemId())
                .optionName(optionItem.getOptionName())
                .price(optionItem.getPrice())
                .isDeleted(optionItem.getIsDeleted())
                .regDate(optionItem.getRegDate())
                .modDate(optionItem.getModDate())
                .build();
    }

    @Override
    // 옵션 값 수정
    public void updateOptionItem(Long shopId, Long optionItemId, UpdateOptionItemDTO updateOptionItemDTO) {

        optionValidator.validateShop(shopId);
        OptionItem optionItem = optionValidator.vlidateOptionItem(optionItemId);

        optionItem.updateFromDTO(updateOptionItemDTO);
    }

    @Override
    // 옵션 값 삭제
    public void deleteOptionItem(Long shopId, Long optionItemId) {

        optionValidator.validateShop(shopId);
        OptionItem optionItem = optionValidator.vlidateOptionItem(optionItemId);

        optionItem.changeIsDeleted(true);
    }
}
