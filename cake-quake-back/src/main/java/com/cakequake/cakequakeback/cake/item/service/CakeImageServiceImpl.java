package com.cakequake.cakequakeback.cake.item.service;


import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeImage;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CakeImageServiceImpl implements CakeImageService {

    private final CakeImageRepository cakeImageRepository;

    @Override
    // 케이크 이미지 저장
    public void saveCakeImages(CakeItem cakeItem, List<ImageDTO> imageUrls) {

        List<CakeImage> imageEntities = new ArrayList<>();
        for (ImageDTO dto : imageUrls) {
            CakeImage image = CakeImage.builder()
                    .cakeItem(cakeItem)
                    .imageUrl(dto.getImageUrl())
                    .isThumbnail(dto.getIsThumbnail())
                    .build();
            imageEntities.add(image);
        }
        cakeImageRepository.saveAll(imageEntities);

    }

    @Override
    @Transactional(readOnly = true)
    // 케이크 이미지 URL 목록 조회
    public List<String> getImageUrls(CakeItem cakeItem) {
        List<CakeImage> images = cakeImageRepository.findByCakeItem(cakeItem);

        List<String> imageUrls = new ArrayList<>();
        for (CakeImage image : images) {
            imageUrls.add(image.getImageUrl());
        }
        return imageUrls;
    }

    @Override
    public void updateCakeImages(CakeItem cakeItem, List<ImageDTO> imageDTOs) {
        List<CakeImage> existingImages = cakeImageRepository.findByCakeItem(cakeItem);

        // 삭제할 이미지 찾기 - 요청받은 이미지 ID 목록 추출
        List<Long> sentImageIds = imageDTOs.stream()
                .map(ImageDTO::getImageId)
                .filter(imageId -> imageId != null)
                .toList();

        // 기존 이미지 중 요청에 없는 이미지는 삭제
        for (CakeImage image : existingImages) {
            if (!sentImageIds.contains(image.getImageId())) {
                cakeImageRepository.delete(image);
            }
        }

        // 기존 이미지 썸네일 변경
        for (CakeImage image : existingImages) {
            for (ImageDTO dto : imageDTOs) {
                if (dto.getImageId() != null && dto.getImageId().equals(image.getImageId())) {
                    image.changeThumbnail(dto.getIsThumbnail());
                    break;
                }
            }
        }

        // 새 이미지 추가
        for (ImageDTO dto : imageDTOs) {
            if (dto.getImageId() == null) {
                CakeImage newImage = CakeImage.builder()
                        .cakeItem(cakeItem)
                        .imageUrl(dto.getImageUrl())
                        .isThumbnail(dto.getIsThumbnail())
                        .build();
                cakeImageRepository.save(newImage);
            }
        }
    }

}
