package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.repo.ShopNoticeRepository;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@Transactional
public class ShopServiceTest {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private ShopNoticeRepository shopNoticeRepository;

    @Autowired
    private ShopService shopService;

    //매장 목록 조회
    @Test
    void ListShops() {
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .page(1)
                .size(5)
                .build();

        ShopStatus status = ShopStatus.Active;

        // when
        InfiniteScrollResponseDTO<ShopPreviewDTO> response =
                shopService.getShopsByStatus(pageRequestDTO, status);

        // then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).isNotNull();
        assertThat(response.getContent().size()).isLessThanOrEqualTo(5); // size 조건 확인
        System.out.println("총 개수: " + response.getTotalCount());
        System.out.println("다음 페이지 존재 여부: " + response.isHasNext());
        for (ShopPreviewDTO dto : response.getContent()) {
            System.out.println("Shop: " + dto.getShopId() + ", Name: " + dto.getShopName());
        }
    }

    //공지사항 목록 조회
    @Test
    void getNoticeList() {
        // given
        Long existingShopId = 2L; // 테스트 DB에 실제 존재하는 shopId로 지정
        PageRequestDTO pageRequestDTO = PageRequestDTO.builder()
                .page(1)
                .size(5)
                .build();

        // when
        InfiniteScrollResponseDTO<ShopNoticeDetailDTO> result =
                shopService.getNoticeList(existingShopId, pageRequestDTO);

        // then
        assertThat(result).isNotNull();
        assertThat(result.getContent().get(0).getShopId()).isEqualTo(existingShopId);
        assertThat(result.getTotalCount()).isGreaterThan(0);

        System.out.println("📢 공지사항 목록:");
        for (ShopNoticeDetailDTO notice : result.getContent()) {
            System.out.println("📝 ID: " + notice.getShopNoticeId());
            System.out.println("    제목: " + notice.getTitle());
            System.out.println("    내용: " + notice.getContent());
            System.out.println("    등록일: " + notice.getRegDate());
            System.out.println("    수정일: " + notice.getModDate());
            System.out.println("    노출여부: " + notice.isVisible());
            System.out.println("----------------------------");
        }

        System.out.println("전체 개수: " + result.getTotalCount());
        System.out.println("다음 페이지 있음? " + result.isHasNext());
    }

    //공지사항 상세 조회
    @Test
    void DetailNotices() {
        // Given
        Long noticeId = 1L; // DB에 존재하는 공지사항 ID

        // When
        ShopNoticeDetailDTO dto = shopService.getNoticeDetail(noticeId);

        // Then
        assertThat(dto).isNotNull();
        System.out.println("============== Notice Detail ==============");
        System.out.println("shopNoticeId : " + dto.getShopNoticeId());
        System.out.println("shopId       : " + dto.getShopId());
        System.out.println("title        : " + dto.getTitle());
        System.out.println("content      : " + dto.getContent());
        System.out.println("isVisible    : " + dto.isVisible());
        System.out.println("regDate      : " + dto.getRegDate());
        System.out.println("modDate      : " + dto.getModDate());
        System.out.println("===========================================");
    }

    //매장 상세 조회
    @Test
    void DetailShops() {
        // given
        Long existingShopId = 2L; // 이 ID에 해당하는 shop이 DB에 있어야 함

        // when
        ShopDetailResponseDTO result = shopService.getShopDetail(existingShopId);

        // then
        assertNotNull(result);
        assertEquals(existingShopId, result.getShopId());
        assertNotNull(result.getShopName());

        // 공지사항 검증
        if (result.getNoticePreview() != null) {
            System.out.println("📢 공지사항 미리보기: " + result.getNoticePreview().getPreviewContent());
            assertTrue(result.getNoticePreview().getPreviewContent().length() <= 33); // ... 포함
        } else {
            System.out.println("📢 공지사항 없음");
        }

        List<CakeListDTO> cakeList = result.getCakes();
        assertNotNull(cakeList);

        System.out.println("\n🎂 케이크 목록:");
        for (CakeListDTO cake : cakeList) {
            System.out.printf(" - [ID: %d] %s | 가격: %d원",
                    cake.getCakeId(),
                    cake.getCname(),
                    cake.getPrice()
            );

            System.out.println("✅ 매장 상세 조회 테스트 완료");
        }
    }
}







