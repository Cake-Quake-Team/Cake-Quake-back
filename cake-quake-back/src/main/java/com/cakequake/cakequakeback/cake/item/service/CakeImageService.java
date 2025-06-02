package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;

import java.util.List;

public interface CakeImageService {

    // 이미지 저장
    void saveCakeImages(CakeItem cakeItem, List<ImageDTO> imageUrls);

    // 이미지 목록 조회
    List<String> getImageUrls(CakeItem cakeItem);

    // 이미지 수정
    void updateCakeImages(CakeItem cakeItem, List<ImageDTO> imageDTOs);
}