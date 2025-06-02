package com.cakequake.cakequakeback.cake.item.controller;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.*;
import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.cake.item.service.CakeItemService;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Log4j2
public class CakeItemController {

    private final CakeItemService cakeItemService;

    // 케이크 전체 목록 조회
    @GetMapping("/cakes")
    public InfiniteScrollResponseDTO<CakeListDTO> getAllCakeList(
            @RequestParam(defaultValue = "LETTERING") CakeCategory keyword,     // 레터링 케이크
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

    @PostMapping("/shops/{shopId}/cakes")
    public ResponseEntity<MappingResponseDTO> addCake(
            @PathVariable Long shopId,
            @RequestBody AddCakeDTO addCakeDTO){

        MappingResponseDTO response = cakeItemService.addCake(addCakeDTO, shopId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/shops/{shopId}/cakes/{cakeId}")
    public MappingResponseDTO updateCake(
            @PathVariable Long shopId,
            @PathVariable Long cakeId,
            @RequestBody UpdateCakeDTO updateCakeDTO) {

        cakeItemService.updateCake(shopId, cakeId, updateCakeDTO);

        return cakeItemService.getCakeDetail(shopId, cakeId);
    }

    @DeleteMapping("shops/{shopId}/cakes/{cakeId}")
    public ResponseEntity<Void> deleteCake(@PathVariable Long shopId, @PathVariable Long cakeId) {
        cakeItemService.deleteCake(shopId, cakeId);
        return ResponseEntity.noContent().build();
    }
}



