package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.service.CakeItemService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.ShopValidator;
import com.cakequake.cakequakeback.shop.dto.*;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopImage;
import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.repo.ShopImageRepository;
import com.cakequake.cakequakeback.shop.repo.ShopNoticeRepository;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class    ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;
    private final ShopNoticeRepository shopNoticeRepository;
    private final CakeItemService cakeItemService;
    private final ShopValidator shopValidator;
    private final ShopImageService shopImageService;
    private final ShopImageRepository shopImageRepository;
    private final GeoService geoService;

    //매장 상세 조회 = 공지사항 미리보기 + 매장별 상품 보기
    @Override
    public ShopDetailResponseDTO getShopDetail(Long shopId) {

        log.info("매장 상세 정보 조회 시작. shopId: {}", shopId);
        // 1. 매장 및 이미지 정보 조회
        Shop shop=shopValidator.validateShop(shopId);
        log.info("✅ 매장 기본 정보 조회 완료 | shopName: {}, rating: {}, reviewCount: {}",
                shop.getShopName(), shop.getRating(), shop.getReviewCount());

        //매장 이미지 정보 조회
        List<ShopImageDTO> images  = shopImageRepository.findShopImages(shopId);
        log.info("🖼️ 매장 이미지 {}개 조회 완료", images.size());
        images.forEach(img -> log.info("   - imageUrl: {}", img.getShopImageUrl()));

        String thumbnailUrl=images.isEmpty()?null:images.get(0).getShopImageUrl();
        log.info("썸네일 URL 설정 완료: {}", thumbnailUrl);

        // 2. 공지사항 미리보기 생성 (기존 로직 유지)
        Optional<ShopNotice> optionalNotice = shopNoticeRepository
                .findLatestByShopId(shopId, PageRequest.of(0, 1))
                .stream().findFirst();

        ShopNoticePreviewDTO previewDTO = optionalNotice.map(notice -> {
            String content = notice.getContent();
            String preview = content.length() <= 30 ? content : content.substring(0, 30) + "...";
            return new ShopNoticePreviewDTO(
                    notice.getShopNoticeId(),
                    shopId,
                    notice.getTitle(),
                    preview,
                    notice.getRegDate(),
                    notice.getModDate()
            );
        }).orElse(null); //공지사항이 없는 경우 null 반환
        log.info("📢 공지사항 미리보기 생성 완료 | 존재 여부: {}", previewDTO != null);
        if (previewDTO != null) {
            log.info("   - title: {}, preview: {}", previewDTO.getTitle(), previewDTO.getPreviewContent());
        }

        // 3. 케이크 목록 조회 (기존 로직 유지)
        PageRequestDTO pageRequestDTO = new PageRequestDTO();
        InfiniteScrollResponseDTO<CakeListDTO> cakes =
                cakeItemService.getShopCakeList(shopId, pageRequestDTO, null);
        log.info("매장 케이크 목록 {}개 조회 완료.", cakes.getContent().size());
        cakes.getContent().forEach(cake ->
                log.info("   - cakeId: {}, name: {}, price: {}, isOnsale: {}, thumbnail: {}",
                        cake.getCakeId(), cake.getCname(), cake.getPrice(), cake.getIsOnsale(), cake.getThumbnailImageUrl())
        );

        // 모든 정보를 최종 DTO에 빌드하여 반환
        ShopDetailResponseDTO responseDTO= ShopDetailResponseDTO.builder()
                .shopId(shop.getShopId())
                .uid(shop.getMember().getUid())
                .businessNumber(shop.getBusinessNumber())
                .shopName(shop.getShopName())
                .address(shop.getAddress())
                .phone(shop.getPhone() != null ? shop.getPhone() : shop.getMember().getPhoneNumber())
                .content(shop.getContent())
                .rating(shop.getRating())
                .reviewCount(shop.getReviewCount())
                .openTime(shop.getOpenTime())
                .closeTime(shop.getCloseTime())
                .closeDays(shop.getCloseDays())
                .websiteUrl(shop.getWebsiteUrl())
                .instagramUrl(shop.getInstagramUrl())
                .status(shop.getStatus())
                .lat(shop.getLat())
                .lng(shop.getLng())
                // 이미지 정보 설정
                .images(images)
                .thumbnailUrl(thumbnailUrl)
                // 추가 정보 설정
                .noticePreview(previewDTO)
                .cakes(cakes.getContent())
                .build(); // 최종적으로 build() 호출

        log.info("✅ ShopDetailResponseDTO 생성 완료 | shopId: {}, shopName: {}, 케이크 수: {}, 이미지 수: {}",
                responseDTO.getShopId(), responseDTO.getShopName(),
                responseDTO.getCakes().size(), responseDTO.getImages().size());
            return responseDTO;
    }


    //매장 목록 조회 -> 필터 관련 로직 추가
    @Override
    public InfiniteScrollResponseDTO<ShopPreviewDTO> getShops(  int page,int size,ShopStatus status,
                                                                String keyword,String filter, String sort) {
        Sort sorting = Sort.by(sort);
        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<ShopPreviewDTO> resultPage;

        boolean hasKeyword = keyword != null && !keyword.trim().isEmpty();
        ShopStatus filterStatus = null; // filterStatus 변수 초기화

        // filter 문자열을 ShopStatus Enum으로 변환 시도
        if (filter != null && !filter.trim().isEmpty()) {
            try {
                filterStatus = ShopStatus.valueOf(filter.toUpperCase()); // 대문자로 변환하여 Enum 매칭 시도
            } catch (IllegalArgumentException e) {
                log.warn(">>> [getShops] Invalid filter status value: " + filter + ". Ignoring filter.", e);
            }
        }
        boolean hasFilter = (filterStatus != null);

        if (hasKeyword || hasFilter) {
            resultPage = shopRepository.findShopPreviewsByStatusAndKeywordAndFilter( status, keyword, filterStatus, pageable);
        } else {
            resultPage = shopRepository.findAll(status, pageable);
        }

        log.info(">>> [getShops] 조회된 매장 수: " + resultPage.getContent().size());
        log.info(">>> [getShops] hasNext: " + resultPage.hasNext());
        log.info(">>> [getShops] totalElements: " + resultPage.getTotalElements());

        return InfiniteScrollResponseDTO.<ShopPreviewDTO>builder()
                .content(resultPage.getContent())
                .hasNext(resultPage.hasNext())
                .totalCount((int) resultPage.getTotalElements())
                .build();
    }


    //공지사항 목록 조회
    @Override
    public InfiniteScrollResponseDTO<ShopNoticeDetailDTO> getNoticeList(Long shopId, PageRequestDTO pageRequestDTO) {
        Pageable pageable = pageRequestDTO.getPageable("regDate"); // 최신순 정렬

        Page<ShopNoticeDetailDTO> page = shopNoticeRepository.findNoticesByShopId(shopId, pageable);

        return InfiniteScrollResponseDTO.<ShopNoticeDetailDTO>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements())
                .build();
    }

    //공지사항 상세 조회
    @Override
    public ShopNoticeDetailDTO getNoticeDetail(Long noticeId) {
        return shopValidator.validateNotice(noticeId);
    }

    //공지사항 추가
    @Override
   public Long createNotice(Long shopId, ShopNoticeDTO noticeDTO){
        Shop shop =shopValidator.validateShop(shopId);

        ShopNotice notice = ShopNotice.builder()
                .shop(shop)
                .title(noticeDTO.getTitle())
                .content(noticeDTO.getContent())
                .build();

        return shopNoticeRepository.save(notice).getShopNoticeId();
    };

    //공지사항 수정
    @Override
    public void updateNotice(Long shopId, Long noticeId, ShopNoticeDTO noticeDTO){

        ShopNotice notice = shopValidator.validateShopNotice(shopId,noticeId);

        notice.update(noticeDTO.getTitle(), noticeDTO.getContent());

    }

    //공지사항 삭제
    @Override
    public void deleteNotice(Long shopId, Long noticeId){

        ShopNotice notice = shopValidator.validateShopNotice(shopId,noticeId);

        shopNoticeRepository.delete(notice);
    }


    //매장 정보 수정
    @Override
    public void updateShop(Long shopId, ShopUpdateDTO updateDTO, List<MultipartFile> files){

        Shop shop = shopValidator.validateShop(shopId);
        shopValidator.validateUpdateShop(updateDTO);

        // 주소가 있으면 좌표 변환 후, 새 DTO 복사본 생성
        if (updateDTO.getAddress() != null && !updateDTO.getAddress().isEmpty()) {
            updateDTO = ShopUpdateDTO.builder()
                    .address(updateDTO.getAddress())
                    .phone(updateDTO.getPhone())
                    .content(updateDTO.getContent())
                    .openTime(updateDTO.getOpenTime())
                    .closeTime(updateDTO.getCloseTime())
                    .closeDays(updateDTO.getCloseDays())
                    .websiteUrl(updateDTO.getWebsiteUrl())
                    .instagramUrl(updateDTO.getInstagramUrl())
                    .status(updateDTO.getStatus())
                    .thumbnailImageUrl(updateDTO.getThumbnailImageUrl())
                    .imageIds(updateDTO.getImageIds())
                    .thumbnailImageId(updateDTO.getThumbnailImageId())
                    .build();
        }

        shop.updateShop(updateDTO);
        Shop saveShop=shopRepository.save(shop);

        ImageResponseDTO saveShopImage=shopImageService.updateShopImages(
                shop,
                updateDTO.getImageIds(),
                files,
                updateDTO.getThumbnailImageId(),
                updateDTO.getThumbnailImageUrl()
        );

        String ThumbnailUrl = saveShopImage.getThumbnailUrl();
        if(ThumbnailUrl!=null){
            saveShop.updateThumbnailImageUrl(ThumbnailUrl);
            shopRepository.save(saveShop);

        }

    }

}










