package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.shop.dto.ShopImageDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ShopImageService {
    void saveShopImages(Shop shop, List<MultipartFile> files);
    List<String> getShopImageUrls(Shop shop);
    void updateShopImages(Shop shop, List<ShopImageDTO> shopImageDtos,List<MultipartFile> files);
}
