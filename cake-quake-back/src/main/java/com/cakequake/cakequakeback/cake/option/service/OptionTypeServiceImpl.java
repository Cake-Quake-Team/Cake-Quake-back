package com.cakequake.cakequakeback.cake.option.service;

import com.cakequake.cakequakeback.cake.option.dto.*;
import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import com.cakequake.cakequakeback.cake.option.repo.OptionItemRepository;
import com.cakequake.cakequakeback.cake.option.repo.OptionTypeRepository;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
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
    public void shopExists(Long shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID);
        }
    }

    // optionTypeId가 존재하지 않을 경우
    private OptionType getOptionTypeOrThrow(Long optionTypeId) {
        return optionTypeRepository.findById(optionTypeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_OPTION_ID));
    }

    @Override
    // 옵션 타입 등록
    public Long addOptionType(Long shopId, AddOptionTypeDTO addOptionTypeDTO) {

        shopExists(shopId);

        OptionType optionType = OptionType.builder()
                .optionType(addOptionTypeDTO.getOptionType())
                .isRequired(addOptionTypeDTO.getIsRequired())
                .minSelection(addOptionTypeDTO.getMinSelection())
                .maxSelection(addOptionTypeDTO.getMaxSelection())
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
                .minSelection(optionType.getMinSelection())
                .maxSelection(optionType.getMaxSelection())
                .build();
    }

    @Override
    // 옵션 타입 수정
    public void updateOptionType(Long shopId, Long optionTypeId, UpdateOptionTypeDTO updateOptionTypeDTO) {

        shopExists(shopId);

        OptionType optionType = getOptionTypeOrThrow(optionTypeId);

        optionType.updateFromDTO(updateOptionTypeDTO);
    }

    @Override
    // 옵션 타입 삭제
    public void deleteOptionType(Long shopId, Long optionTypeId) {

        shopExists(shopId);

        OptionType optionType = getOptionTypeOrThrow(optionTypeId);

        // 옵션 타입에 속해있는 옵션 값들 먼저 삭제
        optionItemRepository.deleteAllByOptionType(optionType);

        optionTypeRepository.delete(optionType);
    }
}
