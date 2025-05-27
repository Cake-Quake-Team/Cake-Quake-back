package com.cakequake.cakequakeback.shop.controller;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import com.cakequake.cakequakeback.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor

public class ShopController {
    private final ShopService shopService;

    //매장 상세 조회
    @GetMapping("/{shopId}")
    public ShopDetailResponseDTO getShopDetail(@PathVariable Long shopId) {
        return shopService.getShopDetail(shopId);
    }
    //매장 목록 조회
    @GetMapping
    public InfiniteScrollResponseDTO<ShopPreviewDTO> getShopsByStatus(
            PageRequestDTO pageRequestDTO,  @RequestParam(defaultValue = "ACTIVE") ShopStatus status){
            return shopService.getShopsByStatus(pageRequestDTO, status);
    }




}
