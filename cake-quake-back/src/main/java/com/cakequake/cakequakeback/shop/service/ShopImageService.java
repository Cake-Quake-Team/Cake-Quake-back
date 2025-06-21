package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.shop.dto.ImageResponseDTO;
import com.cakequake.cakequakeback.shop.dto.ShopImageDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ShopImageService {
    ImageResponseDTO saveShopImages(Shop shop, List<ShopImageDTO> shopImageDTOs, List<MultipartFile> imageFiles, String thumbnailFile);
    ImageResponseDTO updateShopImages(Shop shop, List<Long> imageUrls, List<MultipartFile> imageFiles, Long thumbnailFileId, String thumbnailUrl);
    List<ShopImageDTO> findShop(Shop shop, Long shopId);
    String saveFileAndGetUrl(MultipartFile file);

}
