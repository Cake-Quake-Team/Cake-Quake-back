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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j

public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;
    private final ShopNoticeRepository shopNoticeRepository;
    private final CakeItemService cakeItemService;
    private final ShopValidator shopValidator;
    private final ShopImageService shopImageService;

    //매장 상세 조회 = 공지사항 미리보기 + 매장별 상품 보기
    @Override
    public ShopDetailResponseDTO getShopDetail(Long shopId) {
        // 1. 매장 및 이미지 정보 조회
        List<Object[]> results = shopRepository.SelectDTO(shopId);

        if (results.isEmpty()) {
            throw new EntityNotFoundException("매장을 찾을 수 없습니다.");
        }

        // 쿼리 결과의 첫 번째 행에서 Shop 엔티티를 가져와 기본 DTO를 만듭니다.
        Shop shop = (Shop) results.get(0)[0];
        Member member = shop.getMember();

        // 이미지 정보를 그룹화하고 DTO에 설정
        List<ShopImageDTO> ShopImageDTOS = new ArrayList<>();
        String thumbnailUrl = null;

        for (Object[] row : results) {
            ShopImage shopImage = (ShopImage) row[1];

            //이미지가 없는 경우
            if (shopImage != null) {
                ShopImageDTO imageDTO = ShopImageDTO.builder()
                        .shopImageId(shopImage.getShopImageId())
                        .shopImageUrl(shopImage.getShopImageUrl())
                        .isThumbnail(shopImage.getIsThumbnail())
                        .build();
                ShopImageDTOS.add(imageDTO);

                //썸네일 URL 설정
                if (shopImage.getIsThumbnail() && thumbnailUrl == null) {
                    thumbnailUrl = shopImage.getShopImageUrl();
                }
            }
        }

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
        }).orElse(null);

        // 3. 케이크 목록 조회 (기존 로직 유지)
        PageRequestDTO pageRequestDTO = new PageRequestDTO();
        InfiniteScrollResponseDTO<CakeListDTO> cakes =
                cakeItemService.getShopCakeList(shopId, pageRequestDTO, null);

        // 모든 정보를 최종 DTO에 빌드하여 반환
        return ShopDetailResponseDTO.builder()
                .shopId(shop.getShopId())
                .uid(member.getUid())
                .businessNumber(shop.getBusinessNumber())
                .shopName(shop.getShopName())
                .address(shop.getAddress())
                .phone(shop.getPhone() != null ? shop.getPhone() : member.getPhoneNumber())
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
                .images(ShopImageDTOS)
                .thumbnailUrl(thumbnailUrl)
                // 추가 정보 설정
                .noticePreview(previewDTO)
                .cakes(cakes.getContent())
                .build(); // 최종적으로 build() 호출
    }


    //매장 목록 조회 -> 필터 관련 로직 추가
    @Override
    public InfiniteScrollResponseDTO<ShopPreviewDTO> getShops(  int page,int size,ShopStatus status,
                                                                String keyword,String filter, String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(sort)); // sort 파라미터 사용

        // 2. 검색어(keyword) 및 필터(filter) 적용 로직 추가
        Page<ShopPreviewDTO> resultPage;

        if (keyword != null && !keyword.trim().isEmpty()) {
            resultPage = shopRepository.findAll(status, pageable);
        } else {
            resultPage = shopRepository.findAll(status, pageable);
        }

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

        shop.updateShop(updateDTO);
        shopImageService.updateShopImages(shop, updateDTO.getImageUrls(),files);

    }

}










