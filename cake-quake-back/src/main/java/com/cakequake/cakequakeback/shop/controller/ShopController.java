package com.cakequake.cakequakeback.shop.controller;

import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;
import com.cakequake.cakequakeback.shop.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/shops")
@RequiredArgsConstructor

public class ShopController {
    private final ShopService shopService;

    @GetMapping("/{shopId}")
    public ShopDetailResponseDTO getShopDetail(@PathVariable Long shopId) {
        return shopService.getShopDetail(shopId);
    }
}
