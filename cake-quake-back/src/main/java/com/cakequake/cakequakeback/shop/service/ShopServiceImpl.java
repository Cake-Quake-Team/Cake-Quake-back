package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.service.CakeItemService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.dto.*;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopImage;
import com.cakequake.cakequakeback.shop.entities.ShopNotice;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.repo.ShopNoticeRepository;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor

public class ShopServiceImpl implements ShopService {
    private final ShopRepository shopRepository;
    private final ShopNoticeRepository shopNoticeRepository;
    private final CakeItemService cakeItemService;

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
        List<shopImageDTO> shopImageDTOs = new ArrayList<>();
        String thumbnailUrl = null;

        for (Object[] row : results) {
            ShopImage shopImage = (ShopImage) row[1];

            //이미지가 없는 경우
            if (shopImage != null) {
                shopImageDTO imageDTO = shopImageDTO.builder()
                        .shopImageId(shopImage.getShopImageId())
                        .shopImageUrl(shopImage.getShopImageUrl())
                        .isThumbnail(shopImage.getIsThumbnail())
                        .build();
                shopImageDTOs.add(imageDTO);

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
                .images(shopImageDTOs)
                .thumbnailUrl(thumbnailUrl)
                // 추가 정보 설정
                .noticePreview(previewDTO)
                .cakes(cakes.getContent())
                .build(); // 최종적으로 build() 호출
    }





    //매장 목록 조회
    @Override
    public InfiniteScrollResponseDTO<ShopPreviewDTO> getShopsByStatus(PageRequestDTO pageRequestDTO, ShopStatus status) {
        Pageable pageable = pageRequestDTO.getPageable("shopId"); // 정렬 기준은 필요에 따라 변경

        Page<ShopPreviewDTO> page = shopRepository.findAll(status, pageable);

        return InfiniteScrollResponseDTO.<ShopPreviewDTO>builder()
                .content(page.getContent())
                .hasNext(page.hasNext())
                .totalCount((int) page.getTotalElements())
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
        return shopNoticeRepository.findNoticeDetailById(noticeId)
                .orElseThrow(() -> new EntityNotFoundException("공지사항을 찾을 수 없습니다."));
    }

    //공지사항 추가
    @Override
   public Long createNotice(Long shopId, ShopNoticeDTO noticeDTO){
        Shop shop =shopRepository.findById(shopId)
                .orElseThrow(()-> new EntityNotFoundException("매장을 찾을 수 없습니다."));

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

        ShopNotice notice = shopNoticeRepository.findByShopNoticeIdAndShopShopId(noticeId, shopId)
                .orElseThrow(()-> new EntityNotFoundException("해당 매장의 공지사항을 찾을 수 없습니다."));

        notice.update(noticeDTO.getTitle(), noticeDTO.getContent());

    }

    @Override
    public void deleteNotice(Long shopId, Long noticeId){

        ShopNotice notice = shopNoticeRepository.findByShopNoticeIdAndShopShopId(noticeId, shopId)
                .orElseThrow(()-> new EntityNotFoundException("해당 매장의 공지사항을 찾을 수 없습니다."));


        shopNoticeRepository.delete(notice);
    }


}










