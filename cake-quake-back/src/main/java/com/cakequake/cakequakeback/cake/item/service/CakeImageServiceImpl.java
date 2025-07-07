package com.cakequake.cakequakeback.cake.item.service;

import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.cake.item.dto.ImageResponseDTO;
import com.cakequake.cakequakeback.cake.item.entities.CakeImage;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CakeImageServiceImpl implements CakeImageService {

    private final CakeImageRepository cakeImageRepository;
    private final FileStorageService fileStorageService; // 예: 파일 저장 로직 분리한 서비스

    @Override
    public ImageResponseDTO saveCakeImages(CakeItem cakeItem, List<ImageDTO> imageUrlDTOs, List<MultipartFile> imageFiles, String thumbnailFileName) {
        List<ImageDTO> savedImages = new ArrayList<>();
        String thumbnailUrl = null;

        // 이미지 URL DTO로 저장 (외부 URL 등)
        if (imageUrlDTOs != null) {
            for (ImageDTO dto : imageUrlDTOs) {
                CakeImage cakeImage = CakeImage.builder()
                        .cakeItem(cakeItem)
                        .imageUrl(dto.getImageUrl())
                        .isThumbnail(dto.getIsThumbnail())
                        .build();
                cakeImageRepository.save(cakeImage);

                savedImages.add(new ImageDTO(cakeImage.getImageId(), cakeImage.getImageUrl(), cakeImage.getIsThumbnail()));

                // 썸네일이면 URL 저장
                if (Boolean.TRUE.equals(dto.getIsThumbnail())) {
                    thumbnailUrl = dto.getImageUrl();
                }
            }
        }

        // MultipartFile 이미지 저장
        if (imageFiles != null) {
            for (MultipartFile file : imageFiles) {
                String originalFilename = file.getOriginalFilename();
                String url = saveFileAndGetUrl(file);

                boolean isThumbnail = originalFilename != null && originalFilename.equals(thumbnailFileName);

                CakeImage cakeImage = CakeImage.builder()
                        .cakeItem(cakeItem)
                        .imageUrl(url)
                        .isThumbnail(isThumbnail)
                        .build();
                cakeImageRepository.save(cakeImage);

                savedImages.add(new ImageDTO(cakeImage.getImageId(), cakeImage.getImageUrl(), cakeImage.getIsThumbnail()));

                // 썸네일이면 URL 저장
                if (isThumbnail) {
                    thumbnailUrl = url;
                }
            }
        }

        return new ImageResponseDTO(savedImages, thumbnailUrl);
    }

    @Override
    @Transactional
    public ImageResponseDTO updateCakeImages(
            CakeItem cakeItem,
            List<Long> imageUrlIds,
            List<MultipartFile> newImageFiles,
            Long thumbnailImageId,
            String thumbnailImageUrl
    ) {
        // 기존 이미지 삭제
        List<CakeImage> currentImages = cakeImageRepository.findByCakeItem(cakeItem);
        Set<Long> imageUrlIdSet = new HashSet<>(imageUrlIds != null ? imageUrlIds : new ArrayList<>());

        for (CakeImage img : currentImages) {
            if (img.getImageId() != null && !imageUrlIdSet.contains(img.getImageId())) {
                fileStorageService.deleteFile(img.getImageUrl());
                cakeImageRepository.delete(img);
            }
        }

        // 기존 이미지 썸네일 해제
        List<CakeImage> remainingImages = cakeImageRepository.findByCakeItem(cakeItem);
        for (CakeImage img : remainingImages) {
            if (img.getIsThumbnail()) {
                img.unmarkAsThumbnail();
                cakeImageRepository.save(img);
            }
        }

        // 새 이미지 저장
        Map<String, String> originalToSavedUrl = new HashMap<>();
        List<ImageDTO> savedImageDTOs = new ArrayList<>();

        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            for (MultipartFile file : newImageFiles) {
                String originalFilename = file.getOriginalFilename();
                String savedUrl = saveFileAndGetUrl(file);
                originalToSavedUrl.put(originalFilename, savedUrl);

                CakeImage newCakeImage = CakeImage.builder()
                        .cakeItem(cakeItem)
                        .imageUrl(savedUrl)
                        .isThumbnail(false)
                        .build();

                cakeImageRepository.save(newCakeImage);
                savedImageDTOs.add(ImageDTO.builder()
                        .imageId(newCakeImage.getImageId())
                        .imageUrl(savedUrl)
                        .isThumbnail(false)
                        .build());
            }
        }

        // 썸네일 지정
        String thumbnailUrl  = null;
        if (thumbnailImageId != null) {
            thumbnailUrl  = cakeImageRepository.findById(thumbnailImageId)
                    .map(img -> {
                        img.markAsThumbnail();
                        cakeImageRepository.save(img);
                        return img.getImageUrl();
                    }).orElse(null);
        } else if (thumbnailImageUrl != null) {
            String targetUrl = originalToSavedUrl.get(thumbnailImageUrl);
            if (targetUrl != null) {
                thumbnailUrl  = cakeImageRepository.findByCakeItem(cakeItem).stream()
                        .filter(img -> img.getImageUrl().equals(targetUrl))
                        .findFirst()
                        .map(img -> {
                            img.markAsThumbnail();
                            cakeImageRepository.save(img);
                            return img.getImageUrl();
                        }).orElse(null);
            }
        }

        return ImageResponseDTO.builder()
                .imageDTOs(savedImageDTOs)
                .thumbnailUrl(thumbnailUrl)
                .build();

    }



    @Override
    public String saveFileAndGetUrl(MultipartFile file) {
        // 실제 파일 저장 로직 - 예: 로컬/클라우드 저장 후 URL 반환
        return fileStorageService.storeFile(file, "images/cakeImages/");
    }

    @Override
    @Transactional(readOnly = true)
    // 이미지 조회
    public List<ImageDTO> findByCakeItem(CakeItem cakeItem, Long cakeId) {
        return cakeImageRepository.findCakeImages(cakeId);
    }

}
