package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.entities.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.*;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeImageRepository;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.cake.item.repo.MappingRepository;
import com.cakequake.cakequakeback.cake.option.dto.CakeOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cake.validator.CakeValidator;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class CakeItemServiceImpl implements CakeItemService {

    private final CakeItemRepository cakeItemRepository;
    private final CakeImageRepository cakeImageRepository;
    private final CakeValidator cakeValidator;
    private final CakeImageService cakeImageService;
    private final MappingService mappingService;
    private final MappingRepository mappingRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final ShopRepository shopRepository;


    // 현재 로그인한 사용자의 shopId를 가져오는 메서드
    private Long getCurrentUserShopId() {
        Long currentUid = authenticatedUserService.getCurrentMemberId();
        log.info("현재 로그인된 사용자 UID: {}", currentUid); // 로그 추가

        ShopPreviewDTO shopPreview = shopRepository.findPreviewByUid(currentUid)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));

        log.info("현재 사용자에게 연결된 Shop ID: {}", shopPreview.getShopId()); // 로그 추가
        return shopPreview.getShopId();
    }


    // 현재 로그인한 사용자가 해당 shop의 소유자인지 검증하는 메서드
    private void validateShopOwnership(Long shopId) {
        Long currentUserShopId = getCurrentUserShopId();

        if (!currentUserShopId.equals(shopId)) {
            throw new BusinessException(ErrorCode.NOT_AUTHORIZED_OTHER_SELLER);
        }
    }

    @Override
    // 상품 (옵션 포함) 등록
    public MappingResponseDTO addCake(AddCakeDTO addCakeDTO, List<MultipartFile> cakeImages) {

        // 현재 로그인한 사용자의 shopId 가져오기
        Long shopId = getCurrentUserShopId();

        Shop shop = cakeValidator.validateShop(shopId);
        cakeValidator.validateAddCake(addCakeDTO);
        List<OptionItem> optionItems = cakeValidator.validateOptionItems(addCakeDTO.getMappingRequestDTO().getOptionItemIds());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Member member = cakeValidator.validateMember(userId);

        // 케이크 조회
        CakeItem cake = CakeItem.builder()
                .shop(shop)
                .cname(addCakeDTO.getCname())
                .price(addCakeDTO.getPrice())
                .description(addCakeDTO.getDescription())
                .category(addCakeDTO.getCategory())
                .isOnsale(false)
                .isDeleted(false)
                .viewCount(0)
                .orderCount(0)
                .createdBy(member)
                .modifiedBy(member)
                .build();

        // 케이크 저장
        CakeItem savedCakeItem = cakeItemRepository.save(cake);

        // 이미지 저장
        ImageResponseDTO savedImages = cakeImageService.saveCakeImages(cake, addCakeDTO.getImageUrls() ,cakeImages, addCakeDTO.getThumbnailImageUrl());

        // 썸네일 URL이 있으면 CakeItem 업데이트
        String thumbnailUrl = savedImages.getThumbnailUrl();
        if (thumbnailUrl != null) {
            savedCakeItem.updateThumbnailImageUrl(thumbnailUrl);
        }

        // 옵션 매핑 등록
        mappingService.saveCakeOptionMapping(addCakeDTO.getMappingRequestDTO(), cake, cake.getCakeId(), shopId);

        List<CakeOptionItemDTO> optionItemDTOS = new ArrayList<>();
        for (OptionItem optionItem : optionItems) {
            CakeOptionItemDTO dto = CakeOptionItemDTO.fromEntity(optionItem);
            optionItemDTOS.add(dto);
        }

        log.info("상품이 등록되었습니다. cakeId: {}", savedCakeItem.getCakeId());

        return MappingResponseDTO.builder()
                .cakeDetailDTO(CakeDetailDTO.from(savedCakeItem, savedImages.getImageDTOs()))
                .options(optionItemDTOS)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    // 상품 목록 조회
    public InfiniteScrollResponseDTO<CakeListDTO> getAllCakeList(PageRequestDTO pageRequestDTO, CakeCategory category) {

        cakeValidator.validatePaging(pageRequestDTO);

        // 추천상품 필터링 ("조회순", "최신순", "주문순")
        Sort sort = pageRequestDTO.getSpringSort();
        Pageable pageable = pageRequestDTO.getPageable(sort);

        Page<CakeListDTO> listpage;

        if (category != null) {
            // 특정 카테고리로 필터링된 케이크 목록을 정렬하여 조회
            listpage = cakeItemRepository.findAllCakeList(category, pageable);
        } else if (pageRequestDTO.getKeyword() != null && !pageRequestDTO.getKeyword().isEmpty()) {
            // 키워드 검색이 있다면 키워드로 필터링된 케이크 목록을 정렬하여 조회
            listpage = cakeItemRepository.findAll(pageable)
                    .map(cakeItem -> CakeListDTO.builder()
                            .cakeId(cakeItem.getCakeId())
                            .cname(cakeItem.getCname())
                            .price(cakeItem.getPrice())
                            .thumbnailImageUrl(cakeItem.getThumbnailImageUrl())
                            .isOnsale(cakeItem.getIsOnsale())
                            .viewCount(cakeItem.getViewCount())
                            .orderCount(cakeItem.getOrderCount())
                            .build());
        }
        else {
            // 카테고리나 키워드 없이 전체 케이크 목록을 정렬하여 조회
            listpage = cakeItemRepository.findAll(pageable)
                    .map(cakeItem -> CakeListDTO.builder()
                            .cakeId(cakeItem.getCakeId())
                            .cname(cakeItem.getCname())
                            .price(cakeItem.getPrice())
                            .thumbnailImageUrl(cakeItem.getThumbnailImageUrl())
                            .isOnsale(cakeItem.getIsOnsale())
                            .viewCount(cakeItem.getViewCount())
                            .orderCount(cakeItem.getOrderCount())
                            .shopId(cakeItem.getShop().getShopId())
                            .build());
        }


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
    // 상품 상세 조회
    public MappingResponseDTO getCakeDetail(Long shopId, Long cakeId) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        Member member = cakeValidator.validateMember(userId);
        CakeItem cakeItem = cakeValidator.validateCake(cakeId);

        // 조회수 증가 로직 (판매자가 아닐 때만 증가)
        if (member.getRole().equals(MemberRole.BUYER)) {
                cakeItem.incrementViewCount();
                cakeItemRepository.save(cakeItem);
        }

        List<ImageDTO> images = cakeImageRepository.findCakeImages(cakeId);

        List<OptionItem> optionItems = mappingRepository.findOptionItemsByCakeId(cakeId);

        List<CakeOptionItemDTO> optionsDTOList = optionItems.stream()
                .map(CakeOptionItemDTO::fromEntity)
                .collect(Collectors.toList());

        return MappingResponseDTO.builder()
                .cakeDetailDTO(CakeDetailDTO.from(cakeItem, images))
                .options(optionsDTOList)
                .build();
    }

    @Override
    // 상품 수정
    public void updateCake(Long shopId, Long cakeId, UpdateCakeDTO updateCakeDTO, List<MultipartFile> cakeImages) {

        validateShopOwnership(shopId);

        cakeValidator.validateShop(shopId);
        cakeValidator.validateUpdateCake(updateCakeDTO);
        CakeItem cakeItem = cakeValidator.validateCake(cakeId);

        cakeItem.updateFromDTO(updateCakeDTO);
        CakeItem savedCakeItem = cakeItemRepository.save(cakeItem);

        // 이미지 수정 (변경된 반환 타입에 맞춤)
        ImageResponseDTO savedImage = cakeImageService.updateCakeImages(
                cakeItem,
                updateCakeDTO.getImageIds(),
                cakeImages,
                updateCakeDTO.getThumbnailImageId(),
                updateCakeDTO.getThumbnailImageUrl()
        );

        // 썸네일 URL이 있으면 CakeItem에 업데이트
        String newThumbnailUrl = savedImage.getThumbnailUrl();
        if (newThumbnailUrl != null) {
            savedCakeItem.updateThumbnailImageUrl(newThumbnailUrl);
            cakeItemRepository.save(savedCakeItem);
        }

        // 옵션 매핑 수정
        mappingService.updateCakeOptionMappings(cakeItem, updateCakeDTO.getOptionItemIds());
    }


    @Override
    // 상품 삭제
    public void deleteCake(Long cakeId) {

        CakeItem cakeItem = cakeValidator.validateCake(cakeId);

        // 현재 로그인한 사용자가 해당 케이크의 shop 소유자인지 검증
        validateShopOwnership(cakeItem.getShop().getShopId());

        cakeItem.changeIsDeleted(true);

        // 연관 이미지 삭제
        cakeImageRepository.deleteByCakeItem(cakeItem);

        // 연관 옵션 매핑 삭제
        mappingRepository.deleteByCakeItem(cakeItem);
    }
}
