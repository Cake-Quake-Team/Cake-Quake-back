package com.cakequake.cakequakeback.shop.controller;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.service.CakeItemService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticeDTO;
import com.cakequake.cakequakeback.shop.dto.ShopNoticeDetailDTO;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/shops")
@RequiredArgsConstructor

public class ShopController {
    private final ShopService shopService;
    private final CakeItemService cakeItemService;

    //매장 상세 조회
    @GetMapping("/{shopId}")
    @Transactional(readOnly = true)
    public ShopDetailResponseDTO getShopDetail(@PathVariable Long shopId) {
        return shopService.getShopDetail(shopId);
    }

    //매장 목록 조회
    @GetMapping
    @Transactional(readOnly = true)
    public InfiniteScrollResponseDTO<ShopPreviewDTO> getShopsByStatus(
            PageRequestDTO pageRequestDTO,  @RequestParam(defaultValue = "ACTIVE") ShopStatus status){
            return shopService.getShopsByStatus(pageRequestDTO, status);
    }
    //매장별 케이크 목록 조회
    @GetMapping("/{shopId}/cakes")
    public ResponseEntity<InfiniteScrollResponseDTO<CakeListDTO>> getShopCakes(
            @PathVariable Long shopId,
            PageRequestDTO pageRequestDTO,
            @RequestParam(required = false) CakeCategory category) {

        InfiniteScrollResponseDTO<CakeListDTO> response = cakeItemService.getShopCakeList(shopId, pageRequestDTO, category);
        return ResponseEntity.ok(response);
    }
    // 공지사항 목록 조회 (무한스크롤용)
    @GetMapping("/{shopId}/notices")
    public ResponseEntity<InfiniteScrollResponseDTO<ShopNoticeDetailDTO>> getNotices(
            @PathVariable Long shopId,
            @ModelAttribute PageRequestDTO pageRequestDTO) {

        InfiniteScrollResponseDTO<ShopNoticeDetailDTO> response = shopService.getNoticeList(shopId, pageRequestDTO);
        return ResponseEntity.ok(response);
    }

    // 공지사항 상세 조회
    @GetMapping("/{shopId}/notices/{noticeId}")
    public ResponseEntity<ShopNoticeDetailDTO> getNoticeDetail(@PathVariable Long noticeId) {
        ShopNoticeDetailDTO detail = shopService.getNoticeDetail(noticeId);
        return ResponseEntity.ok(detail);
    }

    //공지사항 생성
    @PostMapping("/{shopId}/notices")
    public ResponseEntity<Long> createNotice(@PathVariable Long shopId,
                                             @RequestBody ShopNoticeDTO dto) {
        Long id = shopService.createNotice(shopId, dto);
        return ResponseEntity.ok(id);
    }

    //공지사항 수정
    @PutMapping("/{shopId}/notices/{noticeId}")
    public ResponseEntity<Void> updateNotice(@PathVariable Long shopId, @PathVariable Long noticeId,
                                             @RequestBody ShopNoticeDTO dto) {
        shopService.updateNotice(shopId,noticeId, dto);
        return ResponseEntity.ok().build();
    }

    //공지사항 삭제
    @DeleteMapping("/{shopId}/notices/{noticeId}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long shopId, @PathVariable Long noticeId) {
        shopService.deleteNotice(shopId, noticeId);
        return ResponseEntity.ok().build();
    }



}






