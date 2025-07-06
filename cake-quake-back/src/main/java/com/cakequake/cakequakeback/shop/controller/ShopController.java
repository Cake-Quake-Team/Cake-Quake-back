package com.cakequake.cakequakeback.shop.controller;

import com.cakequake.cakequakeback.cake.item.entities.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.service.CakeItemService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.dto.*;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/v1/shops")
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
    public InfiniteScrollResponseDTO<ShopPreviewDTO> getShops(PageRequestDTO pageRequestDTO, @RequestParam(defaultValue = "ACTIVE") ShopStatus status, @RequestParam(required = false) String keyword, @RequestParam(required = false) String filter, @RequestParam(required = false, defaultValue = "shopId") String sort) {
        return shopService.getShops(pageRequestDTO.getPage(), pageRequestDTO.getSize(), status, keyword, filter, sort);
    }

    //매장별 케이크 목록 조회
    @GetMapping("/{shopId}/cakes")
    public ResponseEntity<InfiniteScrollResponseDTO<CakeListDTO>> getShopCakes(@PathVariable Long shopId, PageRequestDTO pageRequestDTO, @RequestParam(required = false) CakeCategory category) {
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
    @PatchMapping("/{shopId}/notices/{noticeId}")
    public ResponseEntity<Void> updateNotice(@PathVariable Long shopId, @PathVariable Long noticeId,
                                             @RequestBody ShopNoticeDTO dto) {
        shopService.updateNotice(shopId, noticeId, dto);
        return ResponseEntity.ok().build();
    }

    //공지사항 삭제
    @DeleteMapping("/{shopId}/notices/{noticeId}")
    public ResponseEntity<Void> deleteNotice(@PathVariable Long shopId, @PathVariable Long noticeId) {
        shopService.deleteNotice(shopId, noticeId);
        return ResponseEntity.ok().build();
    }

    //매장 정보 수정
    @PatchMapping("/{shopId}/update")
    public ResponseEntity<Void> updateShop(
            @PathVariable Long shopId,
            @RequestPart(value = "dto", required = false) ShopUpdateDTO dto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files

    ) {
        System.out.println("컨트롤러 updateShop 호출됨. shopId: " + shopId);
        System.out.println("DTO: " + dto); // dto 객체의 toString()이 호출됩니다.
        if (files != null && !files.isEmpty()) { // files가 null이 아니고 비어있지 않은지 확인
            System.out.println("Files 수: " + files.size());
            files.forEach(file -> System.out.println("파일: " + file.getOriginalFilename() + ", 타입: " + file.getContentType()));
        } else {
            System.out.println("Files 없음.");
        }

        // 서비스 계층 호출
        shopService.updateShop(shopId, dto, files);

        // 성공적으로 처리되었음을 나타내는 200 OK 응답 반환 (바디 없음)
        return ResponseEntity.ok().build();


    }
}






