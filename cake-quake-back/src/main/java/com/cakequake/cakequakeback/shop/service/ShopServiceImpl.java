package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.service.CakeItemService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticePreviewDTO;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
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
        // 1. 매장 기본 정보
        ShopDetailResponseDTO dtoBase = shopRepository.selectDTO(shopId)
                .orElseThrow(() -> new EntityNotFoundException("매장을 찾을 수 없습니다."));

        // 2. 공지사항 미리보기 생성
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

        // 3. 케이크 목록 조회
        PageRequestDTO pageRequestDTO = new PageRequestDTO();
        InfiniteScrollResponseDTO<CakeListDTO> cakes =
                cakeItemService.getShopCakeList(shopId, pageRequestDTO, null);

        // 빌더 재활용
        return dtoBase.toBuilder()
                .noticePreview(previewDTO)
                .cakes(cakes.getContent())
                .build();
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

}










