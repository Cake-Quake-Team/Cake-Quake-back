package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.AddCakeDTO;
import com.cakequake.cakequakeback.cake.item.dto.CakeDetailDTO;
import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.dto.UpdateCakeDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeImageRepository;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Log4j2
public class CakeItemServiceImpl implements CakeItemService {

    private final CakeItemRepository cakeItemRepository;
    private final CakeImageRepository cakeImageRepository;
    private final MemberRepository memberRepository;
    private final ShopRepository shopRepository;

    // shopId가 존재하지 않을 경우
    public void shopExists(Long shopId) {
        if (!shopRepository.existsById(shopId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID);
        }
    }

    // cakeId가 존재하지 않을 경우
    public CakeItem cakeItemExists(Long cakeId) {
        return cakeItemRepository.findById(cakeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRODUCT_ID));
    }

    @Override
    // 상품 등록
    public Long addCake(AddCakeDTO addCakeDTO, Long shopId) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));

        if (addCakeDTO.getCname() == null || addCakeDTO.getCname().trim().isEmpty()) {
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

//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//
//        String userId = authentication.getName();

//        Member member = memberRepository.findByUserId(userId)
//                .orElseThrow(() -> new BusinessException(ErrorCode.MISSING_JWT));

        CakeItem cake = CakeItem.builder()
                .shop(shop)
                .cname(addCakeDTO.getCname())
                .price(addCakeDTO.getPrice())
                .description(addCakeDTO.getDescription())
                .category(addCakeDTO.getCategory())
                .isOnsale(false)
                .isDeleted(false)
                .thumbnailImageUrl(addCakeDTO.getThumbnailImageUrl())
                .viewCount(0)
                .orderCount(0)
             //   .createdBy()
             //   .modifiedBy()
                .build();

        CakeItem savedCakeItem = cakeItemRepository.save(cake);

        log.info("상품이 등록되었습니다. cakeId: {}", savedCakeItem.getCakeId());

        return savedCakeItem.getCakeId();
    }

    @Override
    @Transactional(readOnly = true)
    // 상품 전체 목록 조회
    public InfiniteScrollResponseDTO<CakeListDTO> getAllCakeList(PageRequestDTO pageRequestDTO, CakeCategory category) {

        if (pageRequestDTO.getPage() < 1 || pageRequestDTO.getSize() < 1) {
            throw new BusinessException(ErrorCode.INVALID_PAGE_SIZE);
        }

        if (category == null) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY);
        }

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

        shopExists(shopId);

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
    public CakeDetailDTO getCakeDetail(Long shopId, Long cakeId) {

        shopExists(shopId);

        CakeItem cakeItem = cakeItemExists(cakeId);

        List<String> images = cakeImageRepository.findCakeImages(cakeId);

        return CakeDetailDTO.builder()
                .shopId(cakeItem.getShop().getShopId())
                .cakeId(cakeItem.getCakeId())
                .cname(cakeItem.getCname())
                .description(cakeItem.getDescription())
                .price(cakeItem.getPrice())
                .category(cakeItem.getCategory())
                .thumbnailImageUrl(cakeItem.getThumbnailImageUrl())
                .viewCount(cakeItem.getViewCount())
                .orderCount(cakeItem.getOrderCount())
                .isOnsale(cakeItem.getIsOnsale())
                .isDeleted(cakeItem.getIsDeleted())
                .imageUrls(images)
                .regDate(cakeItem.getRegDate())
                .modDate(cakeItem.getModDate())
                .build();
    }

    @Override
    // 상품 수정
    public void updateCake(Long shopId, Long cakeId, UpdateCakeDTO updateCakeDTO) {

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

        log.info("================={}", updateCakeDTO);

        shopExists(shopId);

        CakeItem cakeItem = cakeItemExists(cakeId);

        cakeItem.updateFromDTO(updateCakeDTO);
    }

    @Override
    // 상품 삭제
    public void deleteCake(Long shopId, Long cakeId) {

        shopExists(shopId);

        CakeItem cakeItem = cakeItemExists(cakeId);

        cakeItem.changeIsDeleted(true);
    }
}
