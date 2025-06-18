package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.common.utils.CustomImageUtils;
import com.cakequake.cakequakeback.shop.CustomImagesUtils;
import com.cakequake.cakequakeback.shop.dto.ShopImageDTO;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.entities.ShopImage;
import com.cakequake.cakequakeback.shop.repo.ShopImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j

public class ShopImageServiceImpl implements ShopImageService {
    private final ShopImageRepository shopImageRepository;
    private final CustomImagesUtils imageUtils;

    private static final String UPLOAD_DIR = "C:/nginx-1.26.3/html/shop/Images";
    private static final String BASE_URL="/shop/Images/";

    //매장 이미지 저장
    @Override
    public void saveShopImages(Shop shop, List<MultipartFile> files) {
        List<String> savedFileNames = imageUtils.saveImageFiles(files.toArray(new MultipartFile[0]), UPLOAD_DIR);


        List<ShopImage> images = new ArrayList<>();
        for (String savedFileName : savedFileNames) {
            String imageUrl = BASE_URL + savedFileName;

            ShopImage image = ShopImage.builder()
                    .shop(shop)
                    .shopImageUrl(imageUrl)
                    .isThumbnail(false) // 기본값
                    .build();

            images.add(image);
        }

        shopImageRepository.saveAll(images);
    }

    //매장 이미지 URL 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<String> getShopImageUrls(Shop shop) {
        return shopImageRepository.findByShop(shop)
                .stream()
                .map(ShopImage::getShopImageUrl)
                .toList();
    }

    //이미지 정보 업데이트 (개선된 로직)
    @Override
    public void updateShopImages(Shop shop, List<ShopImageDTO> shopImageDtos, List<MultipartFile> files) {
        log.info("=========== ShopImageServiceImpl.updateShopImages 시작 ===========");
        log.info("대상 Shop ID: {}", shop.getShopId());
        log.info("클라이언트에서 받은 ShopImage DTO 수: {}", (shopImageDtos != null ? shopImageDtos.size() : 0));
        log.info("클라이언트에서 받은 새로운 파일 수: {}", (files != null ? files.size() : 0));

        List<ShopImage> existingImages = shopImageRepository.findByShop(shop);
        log.info("현재 DB에 저장된 기존 이미지 수: {}", existingImages.size());

        // 1. 기존 이미지 처리: 삭제할 이미지와 썸네일 변경할 이미지 식별
        log.info("--- 기존 이미지 처리 시작 ---");
        Set<Long> updatedImageIds = shopImageDtos.stream()
                .filter(dto -> dto.getShopImageId() != null)
                .map(ShopImageDTO::getShopImageId)
                .collect(Collectors.toSet());
        log.debug("클라이언트 DTO에 포함된 (유지될) 이미지 ID 목록: {}", updatedImageIds);


        // 1-1. 삭제 대상 이미지 처리
        log.info("--- 삭제 대상 이미지 식별 및 처리 ---");
        for (ShopImage existingImage : existingImages) {
            if (!updatedImageIds.contains(existingImage.getShopImageId())) {
                log.info("삭제 대상 이미지 발견: ID={}, URL={}", existingImage.getShopImageId(), existingImage.getShopImageUrl());
                try {

                    shopImageRepository.delete(existingImage);
                    log.debug("DB에서 ShopImage 엔티티 삭제 완료: ID={}", existingImage.getShopImageId());

                    imageUtils.deleteImageFile(existingImage.getShopImageUrl(), UPLOAD_DIR, BASE_URL);
                    log.debug("물리적 파일 삭제 완료: URL={}", existingImage.getShopImageUrl());
                } catch (Exception e) {
                    log.error("이미지 삭제 중 오류 발생: ID={}, URL={}, 에러: {}",
                            existingImage.getShopImageId(), existingImage.getShopImageUrl(), e.getMessage(), e);
                    // 특정 이미지 삭제 실패하더라도 다른 이미지 처리는 계속 진행
                    // 필요에 따라 BusinessException을 throw하여 트랜잭션 롤백
                }
            }
        }
        log.info("--- 삭제 대상 이미지 처리 완료 ---");


        // 1-2. 썸네일 상태 업데이트 (shopImageDtos의 정보와 매칭)
        log.info("--- 썸네일 상태 업데이트 처리 시작 ---");
        Map<Long, Boolean> thumbnailStatusMap = shopImageDtos.stream()
                .filter(dto -> dto.getShopImageId() != null)
                .collect(Collectors.toMap(ShopImageDTO::getShopImageId, ShopImageDTO::getIsThumbnail));
        log.debug("클라이언트 DTO에서 받은 썸네일 상태 맵: {}", thumbnailStatusMap);

        for (ShopImage existingImage : existingImages) {
            if (thumbnailStatusMap.containsKey(existingImage.getShopImageId())) {
                Boolean newThumbnailStatus = thumbnailStatusMap.get(existingImage.getShopImageId());
                if (existingImage.getIsThumbnail() != newThumbnailStatus) { // 변경이 있는 경우에만 업데이트
                    existingImage.changeThumbnail(newThumbnailStatus);
                    log.info("이미지 ID {} 썸네일 상태 변경: {} -> {}", existingImage.getShopImageId(), !newThumbnailStatus, newThumbnailStatus);
                    // 변경된 엔티티는 Transactional에 의해 자동 반영되므로 save 불필요 (dirty checking)
                } else {
                    log.debug("이미지 ID {} 썸네일 상태 변경 없음: {}", existingImage.getShopImageId(), newThumbnailStatus);
                }
            }
        }
        log.info("--- 썸네일 상태 업데이트 처리 완료 ---");


        // 2. 새로운 파일 저장 및 DB 등록
        log.info("--- 새로운 파일 저장 및 DB 등록 시작 ---");
        if (files != null && !files.isEmpty()) {
            log.debug("업로드 디렉토리: {}, 베이스 URL: {}", UPLOAD_DIR, BASE_URL);
            // imageUtils.saveImageFiles(MultipartFile[] files, String uploadDir) 시그니처 가정
            // TODO: saveImageFiles 메서드가 List<String>을 반환하도록 수정 필요.
            // 현재는 MultipartFile[]을 받아 List<String>을 반환하는 것으로 가정
            List<String> savedFileNames = imageUtils.saveImageFiles(files.toArray(new MultipartFile[0]), UPLOAD_DIR);
            log.debug("저장된 파일 이름 목록: {}", savedFileNames);

            List<ShopImage> newImages = new ArrayList<>();
            for (int i = 0; i < savedFileNames.size(); i++) {
                String savedFileName = savedFileNames.get(i);
                String imageUrl = BASE_URL + savedFileName;
                MultipartFile originalFile = files.get(i); // 원래 MultipartFile에 접근하여 isThumbnail 정보 등을 얻을 수 있다면 활용

                // 새로운 파일의 썸네일 여부는 DTO에서 명시적으로 받거나, 기본값 처리
                // TODO: ShopImageDTO에 새로운 파일에 대한 썸네일 정보가 있다면 여기서 매핑
                boolean isThumbnailForNewFile = false; // 기본값. 필요시 클라이언트 DTO에서 파싱하도록 로직 추가

                log.info("새 이미지 생성: 파일명={}, URL={}, 썸네일={}", originalFile.getOriginalFilename(), imageUrl, isThumbnailForNewFile);
                ShopImage newImage = ShopImage.builder()
                        .shop(shop) // Shop 엔티티를 직접 참조하도록 변경
                        .shopImageUrl(imageUrl)
                        .isThumbnail(isThumbnailForNewFile)
                        .build();
                newImages.add(newImage);
            }
            shopImageRepository.saveAll(newImages);
            log.info("새로운 이미지 {}개 DB에 저장 완료.", newImages.size());
        } else {
            log.info("새로 업로드할 파일이 없습니다.");
        }
        log.info("--- 새로운 파일 저장 및 DB 등록 완료 ---");

        log.info("=========== ShopImageServiceImpl.updateShopImages 종료 ===========");

    }
}