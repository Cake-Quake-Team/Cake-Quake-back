package com.cakequake.cakequakeback.cake.option.service;

import com.cakequake.cakequakeback.cake.option.dto.*;
import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import com.cakequake.cakequakeback.cake.option.repo.OptionItemRepository;
import com.cakequake.cakequakeback.cake.option.repo.OptionTypeRepository;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class OptionTypeServiceImpl implements OptionTypeService {

    private final OptionTypeRepository optionTypeRepository;
    private final ShopRepository shopRepository;
    private final OptionItemRepository optionItemRepository;

    // shopId가 존재하지 않을 경우
    public Shop shopExists(Long shopId) {
        return shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));

    }

    // optionTypeId가 존재하지 않을 경우
    private OptionType getOptionTypeOrThrow(Long optionTypeId) {
        return optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID));
    }

    @Override
    // 옵션 타입 등록
    public Long addOptionType(Long shopId, AddOptionTypeDTO addOptionTypeDTO) {

        Shop shop = shopExists(shopId);

        if (addOptionTypeDTO.getOptionType() == null || addOptionTypeDTO.getOptionType().trim().isEmpty() || addOptionTypeDTO.getOptionType().length() > 20) {
            throw new BusinessException(ErrorCode.INVALID_TYPE);
        }
//        if (addOptionTypeDTO.getIsRequired() != null && addOptionTypeDTO.getIsRequired() == true) {
//            if (addOptionTypeDTO.getMinSelection() == 0) {
//                throw new BusinessException(ErrorCode.INVALID_SELECTION_WHEN_REQUIRED);
//            }
//        }
//
//        if (addOptionTypeDTO.getMinSelection() != null && addOptionTypeDTO.getMaxSelection() != null) {
//            if (addOptionTypeDTO.getMinSelection() > addOptionTypeDTO.getMaxSelection()) {
//                throw new BusinessException(ErrorCode.INVALID_SELECTION_RANGE);
//            }
//        }

        // isRequired/min/max 기본값 처리
        boolean isRequired = (addOptionTypeDTO.getIsRequired() != null) ? addOptionTypeDTO.getIsRequired() : false;
        int minSelection = (addOptionTypeDTO.getMinSelection() != null) ? addOptionTypeDTO.getMinSelection() : 0;
        int maxSelection = (addOptionTypeDTO.getMaxSelection() != null) ? addOptionTypeDTO.getMaxSelection() : 1;

        OptionType optionType = OptionType.builder()
                .shop(shop)
                .optionType(addOptionTypeDTO.getOptionType())
                .isRequired(isRequired)
                .minSelection(minSelection)
                .maxSelection(maxSelection)
                .isUsed(true)
                .isDeleted(false)
                .build();

        OptionType savedOptionType = optionTypeRepository.save(optionType);

        log.info("옵션 타입이 등록되었습니다. 옵션타입 : " + optionType.getOptionType());

        return savedOptionType.getOptionTypeId();
    }

    @Override
    @Transactional(readOnly = true)
    // 옵션 타입 목록 조회
    public InfiniteScrollResponseDTO<CakeOptionTypeDTO> getOptionTypeList(PageRequestDTO pageRequestDTO, Long shopId) {

        shopExists(shopId);

        if (pageRequestDTO.getPage() < 1 || pageRequestDTO.getSize() < 1) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }

        Pageable pageable = pageRequestDTO.getPageable("regDate");

        Page<CakeOptionTypeDTO> typeListPage = optionTypeRepository.findOptionType(shopId ,pageable);

        return InfiniteScrollResponseDTO.<CakeOptionTypeDTO>builder()
                .content(typeListPage.getContent())
                .hasNext(typeListPage.hasNext())
                .totalCount((int) typeListPage.getTotalElements())
                .build();
    }

    @Override
    // 옵션 타입 상세 조회
    public OptionTypeDetailDTO getOptionTypeDetail(Long shopId, Long optionTypeId) {

        shopExists(shopId);

        OptionType optionType = getOptionTypeOrThrow(optionTypeId);

        return OptionTypeDetailDTO.builder()
                .optionTypeId(optionType.getOptionTypeId())
                .optionType(optionType.getOptionType())
                .isRequired(optionType.getIsRequired())
                .isUsed(optionType.getIsUsed())
                .minSelection(optionType.getMinSelection())
                .maxSelection(optionType.getMaxSelection())
                .regDate(optionType.getRegDate())
                .modDate(optionType.getModDate())
                .build();
    }

    @Override
    // 옵션 타입 수정
    public void updateOptionType(Long shopId, Long optionTypeId, UpdateOptionTypeDTO updateOptionTypeDTO) {

        shopExists(shopId);

        if (updateOptionTypeDTO.getOptionType() != null){
           if(updateOptionTypeDTO.getOptionType().trim().isEmpty() || updateOptionTypeDTO.getOptionType().length() > 20
           ) {
               throw new BusinessException(ErrorCode.INVALID_TYPE);
           }
        }
//        if (updateOptionTypeDTO.getIsRequired() != null && updateOptionTypeDTO.getIsRequired()) {
//            if (updateOptionTypeDTO.getMinSelection() == 0) {
//                throw new BusinessException(ErrorCode.INVALID_SELECTION_WHEN_REQUIRED);
//            }
//        }
//        if (updateOptionTypeDTO.getMinSelection() != null && updateOptionTypeDTO.getMaxSelection() != null) {
//            if (updateOptionTypeDTO.getMinSelection() > updateOptionTypeDTO.getMaxSelection()) {
//                throw new BusinessException(ErrorCode.INVALID_SELECTION_RANGE);
//            }
//        }

        OptionType optionType = getOptionTypeOrThrow(optionTypeId);

        optionType.updateFromDTO(updateOptionTypeDTO);
    }

    @Override
    // 옵션 타입 삭제
    public void deleteOptionType(Long shopId, Long optionTypeId) {

        shopExists(shopId);

        OptionType optionType = getOptionTypeOrThrow(optionTypeId);

        // 옵션 타입에 속해있는 옵션 값들 먼저 삭제
        optionItemRepository.markAllDeletedByOptionType(optionType);

        optionType.changeIsDeleted(true);
    }
}
