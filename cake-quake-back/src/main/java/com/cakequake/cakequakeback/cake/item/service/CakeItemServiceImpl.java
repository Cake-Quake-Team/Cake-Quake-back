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

    @Override
    // 상품 등록
    public Long addCake(AddCakeDTO addCakeDTO, Long shopId, Long uid) {

        Shop shop = shopRepository.findById(shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_SHOP_ID));
        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

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
                .createdBy(member)
                .modifiedBy(member)
                .build();

        CakeItem savedCakeItem = cakeItemRepository.save(cake);

        log.info("상품이 등록되었습니다. cakeId: {}", savedCakeItem.getCakeId());

        return savedCakeItem.getCakeId();
    }

    @Override
    @Transactional(readOnly = true)
    // 상품 전체 목록 조회
    public InfiniteScrollResponseDTO<CakeListDTO> getAllCakeList(PageRequestDTO pageRequestDTO, CakeCategory category) {

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
    public CakeDetailDTO getCakeDetail(Long cakeId) {

        CakeItem cakeItem = cakeItemRepository.findCakeDetail(cakeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRODUCT_ID));

        List<String> images = cakeImageRepository.findCakeImages(cakeId);

        return CakeDetailDTO.builder()
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
                .build();
    }

    @Override
    // 상품 수정
    public void updateCake(Long cakeId, UpdateCakeDTO updateCakeDTO) {

        CakeItem cakeItem = cakeItemRepository.findById(cakeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRODUCT_ID));

        cakeItem.changeCname(updateCakeDTO.getCname());
        cakeItem.changePrice(updateCakeDTO.getPrice());
        cakeItem.changeDescription(updateCakeDTO.getDescription());
        cakeItem.changeCategory(updateCakeDTO.getCategory());
        cakeItem.changeThumbnailImageUrl(updateCakeDTO.getThumbnailImageUrl());
        cakeItem.changeIsOnsale(updateCakeDTO.getIsOnsale());

        cakeItemRepository.save(cakeItem);
    }

    @Override
    // 상품 삭제
    public void deleteCake(Long cakeId) {

        CakeItem cakeItem = cakeItemRepository.findById(cakeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRODUCT_ID));

        cakeItem.changeIsDeleted(true);

        cakeItemRepository.save(cakeItem);
    }
}
