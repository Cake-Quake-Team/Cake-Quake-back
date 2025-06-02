package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.*;
import com.cakequake.cakequakeback.cake.item.entities.CakeImage;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeImageRepository;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.cake.item.repo.MappingRepository;
import com.cakequake.cakequakeback.cake.option.dto.CakeOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cake.option.service.OptionItemService;
import com.cakequake.cakequakeback.cake.validator.CakeValidator;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class CakeItemServiceImpl implements CakeItemService {

    private final CakeItemRepository cakeItemRepository;
    private final CakeImageRepository cakeImageRepository;
    private final CakeValidator cakeValidator;
    private final CakeImageService cakeImageService;
    private final OptionItemService optionItemService;
    private final MappingService cakeOptionMappingService;
    private final MappingRepository mappingRepository;

    @Override
    // 상품 (옵션 포함) 등록
    public MappingResponseDTO addCake(AddCakeDTO addCakeDTO, Long shopId) {

        Shop shop = cakeValidator.validateShop(shopId);
        cakeValidator.validateAddCake(addCakeDTO);
        String thumbnailImageUrl = cakeValidator.validateThumbnailImageUrl(addCakeDTO.getImageUrls());
        List<OptionItem> optionItems = cakeValidator.validateOptionItems(addCakeDTO.getMappingRequestDTO().getOptionItemIds());

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        String userId = authentication.getName();
//
//        Member member = cakeValidator.validateMember(userId);

        // 케이크 조회
        CakeItem cake = CakeItem.builder()
                .shop(shop)
                .cname(addCakeDTO.getCname())
                .price(addCakeDTO.getPrice())
                .description(addCakeDTO.getDescription())
                .category(addCakeDTO.getCategory())
                .isOnsale(false)
                .isDeleted(false)
                .thumbnailImageUrl(thumbnailImageUrl)
                .viewCount(0)
                .orderCount(0)
                //   .createdBy(member)
                //   .modifiedBy(member)
                .build();

        // 케이크 저장
        CakeItem savedCakeItem = cakeItemRepository.save(cake);

        // 이미지 저장
        cakeImageService.saveCakeImages(savedCakeItem, addCakeDTO.getImageUrls());

        // 옵션 매핑 저장
        cakeOptionMappingService.saveCakeOptionMapping(savedCakeItem, optionItems);

        List<CakeOptionItemDTO> optionItemDTOS = new ArrayList<>();
        for (OptionItem optionItem : optionItems) {
            CakeOptionItemDTO dto = CakeOptionItemDTO.fromEntity(optionItem);
            optionItemDTOS.add(dto);
        }

        log.info("상품이 등록되었습니다. cakeId: {}", savedCakeItem.getCakeId());

        return MappingResponseDTO.builder()
                .cakeDetailDTO(CakeDetailDTO.from(savedCakeItem, addCakeDTO.getImageUrls()))
                .options(optionItemDTOS)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    // 상품 전체 목록 조회
    public InfiniteScrollResponseDTO<CakeListDTO> getAllCakeList(PageRequestDTO pageRequestDTO, CakeCategory category) {

        cakeValidator.validatePaging(pageRequestDTO, category);

        Pageable pageable = pageRequestDTO.getPageable("regDate");  // 최신순 정렬 등

        Page<CakeListDTO> listpage = cakeItemRepository.findAllCakeList(category, pageable);

        return InfiniteScrollResponseDTO.<CakeListDTO>builder()
                .content(listpage.getContent())                 // 현재 페이지 상품 목록
                .hasNext(listpage.hasNext())                    // 다음 페이지 여부
                .totalCount((int) listpage.getTotalElements())  // 페이지 구분없이 전체 케이크 개수
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    // 특정 매장의 상품 목록 조회
    public InfiniteScrollResponseDTO<CakeListDTO> getShopCakeList(Long shopId, PageRequestDTO pageRequestDTO, CakeCategory category) {

        cakeValidator.validateShop(shopId);

        Pageable pageable = pageRequestDTO.getPageable("regDate");  // 최신순 정렬 등

        Page<CakeListDTO> listpage = cakeItemRepository.findShopCakeList(shopId, category, pageable);

        return InfiniteScrollResponseDTO.<CakeListDTO>builder()
                .content(listpage.getContent())                 // 현재 페이지 상품 목록
                .hasNext(listpage.hasNext())                    // 다음 페이지 여부
                .totalCount((int) listpage.getTotalElements())  // 페이지 구분없이 전체 케이크 개수
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    // 상품 상세 조회
    public MappingResponseDTO getCakeDetail(Long shopId, Long cakeId) {

        cakeValidator.validateShop(shopId);

        CakeItem cakeItem = cakeValidator.validateCake(cakeId);

        List<ImageDTO> images = cakeImageRepository.findCakeImages(cakeId);

        List<OptionItem> optionItems = mappingRepository.findOptionItemsByCakeId(cakeId);

        List<CakeOptionItemDTO> optionItemDTOs = new ArrayList<>();
        for (OptionItem optionItem : optionItems) {
            CakeOptionItemDTO dto = CakeOptionItemDTO.fromEntity(optionItem);
            optionItemDTOs.add(dto);
        }

        return MappingResponseDTO.builder()
                .cakeDetailDTO(CakeDetailDTO.from(cakeItem, images))
                .options(optionItemDTOs)
                .build();
    }

    @Override
    // 상품 수정
    public void updateCake(Long shopId, Long cakeId, UpdateCakeDTO updateCakeDTO) {

        cakeValidator.validateShop(shopId);

        cakeValidator.validateUpdateCake(updateCakeDTO);

        CakeItem cakeItem = cakeValidator.validateCake(cakeId);

        log.info("================={}", updateCakeDTO);

        cakeItem.updateFromDTO(updateCakeDTO);

        // 이미지 수정
        cakeImageService.updateCakeImages(cakeItem, updateCakeDTO.getImageUrls());

        // 옵션 매핑 수정
        cakeOptionMappingService.updateCakeOptionMappings(cakeItem, updateCakeDTO.getOptionItemIds());
    }

    @Override
    // 상품 삭제
    public void deleteCake(Long shopId, Long cakeId) {

        cakeValidator.validateShop(shopId);

        CakeItem cakeItem = cakeValidator.validateCake(cakeId);

        cakeItem.changeIsDeleted(true);

        // 연관 이미지 삭제
        List<CakeImage> images = cakeImageRepository.findByCakeItem(cakeItem);
        cakeImageRepository.deleteAll(images);

        // 연관 옵션 매핑 삭제
        mappingRepository.deleteByCakeItem(cakeItem);
    }
}
