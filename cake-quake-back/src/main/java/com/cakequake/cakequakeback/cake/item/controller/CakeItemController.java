package com.cakequake.cakequakeback.cake.item.controller;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import com.cakequake.cakequakeback.cake.item.dto.AddCakeDTO;
import com.cakequake.cakequakeback.cake.item.dto.CakeDetailDTO;
import com.cakequake.cakequakeback.cake.item.dto.CakeListDTO;
import com.cakequake.cakequakeback.cake.item.dto.UpdateCakeDTO;
import com.cakequake.cakequakeback.cake.item.service.CakeItemService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Log4j2
public class CakeItemController {

    private final CakeItemService cakeItemService;

    @GetMapping("/cakes")
    public InfiniteScrollResponseDTO<CakeListDTO> getAllCakeList(
            @RequestParam(defaultValue = "LETTERING") CakeCategory category,     // 레터링 케이크
            PageRequestDTO pageRequestDTO) {
        return cakeItemService.getAllCakeList(pageRequestDTO, category);
    }

    @GetMapping("/cakes/{cakeId}")
    public CakeDetailDTO getCakeDetail(@PathVariable Long cakeId) {
        return cakeItemService.getCakeDetail(cakeId);
    };

    @PostMapping("/shops/{shopId}/cakes")
    public ResponseEntity<CakeDetailDTO> addCake(
            @PathVariable Long shopId,
            @RequestBody AddCakeDTO addCakeDTO){

        Long cakeId = cakeItemService.addCake(addCakeDTO, shopId, addCakeDTO.getUid());

        CakeDetailDTO response = CakeDetailDTO.builder()
                .cakeId(cakeId)
                .shopId(shopId)
                .createdAt(LocalDateTime.now())
                .build();
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/shops/{shopId}/cakes/{cakeId}")
    public CakeDetailDTO updateCake(
            @PathVariable Long shopId,
            @PathVariable Long cakeId,
            @RequestBody UpdateCakeDTO updateCakeDTO) {

        cakeItemService.updateCake(shopId, cakeId, updateCakeDTO);

        return cakeItemService.getCakeDetail(cakeId);
    }

    @DeleteMapping("shops/{shopId}/cakes/{cakeId}")
    public ResponseEntity<Void> deleteCake(@PathVariable Long shopId, @PathVariable Long cakeId) {
        cakeItemService.deleteCake(shopId, cakeId);
        return ResponseEntity.noContent().build();
    }
}



