package com.cakequake.cakequakeback.cake.item.controller;

import com.cakequake.cakequakeback.cake.item.entities.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.*;
import com.cakequake.cakequakeback.cake.item.service.CakeItemService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Log4j2
public class CakeItemController {

    private final CakeItemService cakeItemService;

    // 케이크 전체 목록 조회
    @GetMapping("/cakes")
    public InfiniteScrollResponseDTO<CakeListDTO> getAllCakeList(
            @RequestParam(required = false) CakeCategory keyword,
            PageRequestDTO pageRequestDTO) {
        return cakeItemService.getAllCakeList(pageRequestDTO, keyword);
    }


    // 케이크 상세 조회
    @GetMapping("/shops/{shopId}/cakes/{cakeId}")
    public MappingResponseDTO getCakeDetail(
            @PathVariable Long shopId,
            @PathVariable Long cakeId) {

        return cakeItemService.getCakeDetail(shopId, cakeId);
    };

    // 케이크 등록
    @PostMapping("/cakes")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<MappingResponseDTO> addCake(
            @RequestPart(value = "addCakeDTO") AddCakeDTO addCakeDTO,
            @RequestPart(value = "cakeImages", required = false) List<MultipartFile> cakeImages){

        MappingResponseDTO response = cakeItemService.addCake(addCakeDTO, cakeImages);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 케이크 수정
    @PatchMapping("/shops/{shopId}/cakes/{cakeId}")
    @PreAuthorize("hasRole('SELLER')")
    public MappingResponseDTO updateCake(
            @PathVariable Long shopId,
            @PathVariable Long cakeId,
            @RequestPart(value = "updateCakeDTO") UpdateCakeDTO updateCakeDTO,
            @RequestPart(value = "newCakeImages", required = false) List<MultipartFile> newCakeImages,
            @RequestPart(value = "thumbnailImageUrl", required = false) MultipartFile thumbnailImageUrl) {

        System.out.println("썸네일 URL: " + thumbnailImageUrl);

        cakeItemService.updateCake(shopId, cakeId, updateCakeDTO, newCakeImages);

        return cakeItemService.getCakeDetail(cakeId, shopId);
    }

    // 케이크 삭제
    @DeleteMapping("/cakes/{cakeId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteCake(@PathVariable Long cakeId) {
        cakeItemService.deleteCake(cakeId);
        return ResponseEntity.noContent().build();
    }
}



