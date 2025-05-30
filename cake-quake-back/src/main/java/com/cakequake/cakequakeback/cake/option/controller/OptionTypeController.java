package com.cakequake.cakequakeback.cake.option.controller;

import com.cakequake.cakequakeback.cake.option.dto.AddOptionTypeDTO;
import com.cakequake.cakequakeback.cake.option.dto.CakeOptionTypeDTO;
import com.cakequake.cakequakeback.cake.option.dto.OptionTypeDetailDTO;
import com.cakequake.cakequakeback.cake.option.dto.UpdateOptionTypeDTO;
import com.cakequake.cakequakeback.cake.option.service.OptionTypeService;
import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shops/{shopId}/options/types")
@RequiredArgsConstructor
public class OptionTypeController {

    private final OptionTypeService optionTypeService;

    @GetMapping
    public InfiniteScrollResponseDTO<CakeOptionTypeDTO> getOptionTypeList(
            @PathVariable Long shopId,
            PageRequestDTO pageRequestDTO) {

        return optionTypeService.getOptionTypeList(pageRequestDTO, shopId);
    }

    @GetMapping("/{optionTypeId}")
    public OptionTypeDetailDTO getOptionTypeDetail(
            @PathVariable Long shopId,
            @PathVariable Long optionTypeId) {

        return optionTypeService.getOptionTypeDetail(shopId, optionTypeId);
    }

    @PostMapping
    public ResponseEntity<CakeOptionTypeDTO> addOptionType(
            @PathVariable Long shopId,
            @RequestBody AddOptionTypeDTO addOptionTypeDTO) {

        Long optionTypeId = optionTypeService.addOptionType(shopId, addOptionTypeDTO);

        CakeOptionTypeDTO response = CakeOptionTypeDTO.builder()
                .optionTypeId(optionTypeId)
                .optionType(addOptionTypeDTO.getOptionType())
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{optionTypeId}")
    public OptionTypeDetailDTO updateOptionType(
            @PathVariable Long shopId,
            @PathVariable Long optionTypeId,
            @RequestBody UpdateOptionTypeDTO updateOptionTypeDTO) {

        optionTypeService.updateOptionType(shopId, optionTypeId, updateOptionTypeDTO);

        return optionTypeService.getOptionTypeDetail(shopId, optionTypeId);
    }

    @DeleteMapping("/{optionTypeId}")
    public ResponseEntity<Void> deleteOptionType(
            @PathVariable Long shopId,
            @PathVariable Long optionTypeId) {

        optionTypeService.deleteOptionType(shopId, optionTypeId);

        return ResponseEntity.noContent().build();
    }
}