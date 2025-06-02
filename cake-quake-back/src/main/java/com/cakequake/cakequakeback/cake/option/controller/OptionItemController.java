package com.cakequake.cakequakeback.cake.option.controller;

import com.cakequake.cakequakeback.cake.option.dto.AddOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.dto.CakeOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.dto.OptionItemDetailDTO;
import com.cakequake.cakequakeback.cake.option.dto.UpdateOptionItemDTO;
import com.cakequake.cakequakeback.cake.option.service.OptionItemService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shops/{shopId}/options/items")
@RequiredArgsConstructor
public class OptionItemController {

    private final OptionItemService optionItemService;

    @GetMapping
    public InfiniteScrollResponseDTO<CakeOptionItemDTO> getOptionItemList(
            @PathVariable Long shopId,
            PageRequestDTO pageRequestDTO) {

        return optionItemService.getOptionItemList(shopId, pageRequestDTO);
    }

    @GetMapping("/{optionItemId}")
    public OptionItemDetailDTO getOptionItemDetail(
            @PathVariable Long shopId,
            @PathVariable Long optionItemId) {

        return optionItemService.getOptionItemDetail(shopId, optionItemId);
    }

    @PostMapping
    public ResponseEntity<CakeOptionItemDTO> addOptionItem(
            @PathVariable Long shopId,
            @RequestBody AddOptionItemDTO addOptionItemDTO) {

        Long optionItemId = optionItemService.addOptionItem(shopId, addOptionItemDTO);

        CakeOptionItemDTO response = CakeOptionItemDTO.builder()
                .optionItemId(optionItemId)
                .optionName(addOptionItemDTO.getOptionName())
                .price(addOptionItemDTO.getPrice())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{optionItemId}")
    public OptionItemDetailDTO updateOptionItem(
            @PathVariable Long shopId,
            @PathVariable Long optionItemId,
            @RequestBody UpdateOptionItemDTO updateOptionItemDTO) {

        optionItemService.updateOptionItem(shopId, optionItemId, updateOptionItemDTO);

        return optionItemService.getOptionItemDetail(shopId, optionItemId);
    }

    @DeleteMapping("/{optionItemId}")
    public ResponseEntity<Void> deleteOptionItem(
            @PathVariable Long shopId,
            @PathVariable Long optionItemId) {

        optionItemService.deleteOptionItem(shopId, optionItemId);

        return ResponseEntity.noContent().build();
    }
}
