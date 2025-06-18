package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.cake.item.dto.ImageResponseDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeImage;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface CakeImageService {

    // 이미지 저장 (신규 등록 시)
    ImageResponseDTO saveCakeImages(CakeItem cakeItem, List<ImageDTO> imageUrlDTOs, List<MultipartFile> imageFiles, String thumbnailFileName);

    // 이미지 수정 (기존 이미지 삭제 + 새 이미지 저장)
    ImageResponseDTO updateCakeImages(CakeItem cakeItem, List<Long> imageUrlIds, List<MultipartFile> newImageFiles, Long thumbnailImageId, String newThumbnailFileName);

    // 단순 파일 저장 후 URL 반환
    String saveFileAndGetUrl(MultipartFile file);

    // 이미지 조회
    List<ImageDTO> findByCakeItem(CakeItem cakeItem, Long cakeId);
}
