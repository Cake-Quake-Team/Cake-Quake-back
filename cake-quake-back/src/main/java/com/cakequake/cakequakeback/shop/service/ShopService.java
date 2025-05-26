package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.shop.dto.ShopDetailResponseDTO;

public interface ShopService {
    ShopDetailResponseDTO getShopDetail(Long shopId);
}
