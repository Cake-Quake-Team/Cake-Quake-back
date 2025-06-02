package com.cakequake.cakequakeback.cake.option.service;

import com.cakequake.cakequakeback.cake.option.dto.*;
import com.cakequake.cakequakeback.cake.option.entities.OptionType;
import com.cakequake.cakequakeback.cake.option.repo.OptionItemRepository;
import com.cakequake.cakequakeback.cake.option.repo.OptionTypeRepository;
import com.cakequake.cakequakeback.cake.validator.OptionValidator;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import lombok.RequiredArgsConstructor;
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
public class OptionTypeServiceImpl implements OptionTypeService {

    private final OptionTypeRepository optionTypeRepository;
    private final OptionItemRepository optionItemRepository;
    private final OptionValidator optionValidator;

    @Override
    // 옵션 타입 등록
    public Long addOptionType(Long shopId, AddOptionTypeDTO addOptionTypeDTO) {

        Shop shop = optionValidator.validateShop(shopId);
        optionValidator.validateAddOptionType(addOptionTypeDTO);

        // 삭제된 동일 이름의 OptionType이 존재하는지 확인
        Optional<OptionType> alreadyDeleted = optionTypeRepository.findByOptionTypeAndIsDeletedTrue(addOptionTypeDTO.getOptionType());

        if (alreadyDeleted.isPresent()) {
            OptionType deleted = alreadyDeleted.get();
            deleted.restoreOptionType(addOptionTypeDTO);

            return deleted.getOptionTypeId();
        }

        OptionType optionType = OptionType.builder()
                .shop(shop)
                .optionType(addOptionTypeDTO.getOptionType())
                .isRequired(addOptionTypeDTO.getIsRequired())
                .minSelection(addOptionTypeDTO.getMinSelection())
                .maxSelection(addOptionTypeDTO.getMaxSelection())
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

        optionValidator.validateShop(shopId);
        optionValidator.validatePaging(pageRequestDTO);

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

        optionValidator.validateShop(shopId);
        OptionType optionType = optionValidator.validateOptionType(optionTypeId);

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

        optionValidator.validateShop(shopId);
        OptionType optionType = optionValidator.validateOptionType(optionTypeId);
        optionValidator.validateUpdateOptionType(updateOptionTypeDTO);

        optionType.updateFromDTO(updateOptionTypeDTO);
    }

    @Override
    // 옵션 타입 삭제
    public void deleteOptionType(Long shopId, Long optionTypeId) {

        optionValidator.validateShop(shopId);
        OptionType optionType = optionValidator.validateOptionType(optionTypeId);

        // 옵션 타입에 속해있는 옵션 값들 먼저 삭제
        optionItemRepository.markAllDeletedByOptionType(optionType);

        optionType.changeIsDeleted(true);
    }
}
