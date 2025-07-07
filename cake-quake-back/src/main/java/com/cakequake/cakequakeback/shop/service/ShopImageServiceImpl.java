package com.cakequake.cakequakeback.shop.service;

import com.cakequake.cakequakeback.cake.item.service.FileStorageService;
import com.cakequake.cakequakeback.shop.dto.ImageResponseDTO;
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

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j

public class ShopImageServiceImpl implements ShopImageService {
    private final ShopImageRepository shopImageRepository;
    private final FileStorageService fileStorageService;

    //매장 이미지 저장
    @Override
    public ImageResponseDTO saveShopImages(Shop shop, List<ShopImageDTO> shopImageDTOs, List<MultipartFile> imageFiles, String thumbnailFile) {
        List<ShopImageDTO> savedShopImages = new ArrayList<>();
        String thumbnailUrl = null;

        if(shopImageDTOs != null) {
            for(ShopImageDTO dto : shopImageDTOs) {
                ShopImage shopImage = ShopImage.builder()
                        .shop(shop)
                        .shopImageUrl(dto.getShopImageUrl())
                        .isThumbnail(dto.getIsThumbnail())
                        .build();

                shopImageRepository.save(shopImage);

                savedShopImages.add(new ShopImageDTO(shopImage.getShopImageId(), dto.getShopImageUrl(), dto.getIsThumbnail()));

                if(Boolean.TRUE.equals(dto.getIsThumbnail())) {
                    thumbnailUrl=dto.getShopImageUrl();
                } //end if
            } //end for
        } //end if

        if(imageFiles != null) {
            for(MultipartFile file : imageFiles) {
                String originalFilename = file.getOriginalFilename();
                String url = saveFileAndGetUrl(file);

                boolean isThumbnail = originalFilename != null && originalFilename.equals(thumbnailFile);

                ShopImage shopImage = ShopImage.builder()
                        .shop(shop)
                        .shopImageUrl(url)
                        .isThumbnail(isThumbnail)
                        .build();
                shopImageRepository.save(shopImage);

                savedShopImages.add(new ShopImageDTO(shopImage.getShopImageId(), url, isThumbnail));

                if(isThumbnail) {
                    thumbnailUrl=url;
                }//end if

            }//end for
        }//end if

        return new ImageResponseDTO(savedShopImages, thumbnailUrl);
    }

    //이미지 정보 업데이트 (개선된 로직)
    @Override
    public ImageResponseDTO updateShopImages(Shop shop, List<Long> imageUrls, List<MultipartFile> newImageFiles, Long thumbnailFileId, String thumbnailUrl) {


        log.info("DEBUG: [ShopImageService] updateShopImages 메서드 시작. shopId: " + shop.getShopId());
        log.info("DEBUG: [ShopImageService] 유지할 이미지 ID 목록 (imageUrls): " + (imageUrls != null ? imageUrls.toString() : "null"));
        log.info("DEBUG: [ShopImageService] 새로 업로드될 파일 수 (newImageFiles): " + (newImageFiles != null ? newImageFiles.size() : 0));
        log.info("DEBUG: [ShopImageService] 썸네일 지정 ID (thumbnailFileId): " + thumbnailFileId);
        log.info("DEBUG: [ShopImageService] DTO에서 넘어온 썸네일 URL/이름 (thumbnailUrlFromDTO): " + thumbnailUrl);

        List<ShopImage> shopImages = shopImageRepository.findByShop(shop);
        log.info("DEBUG: [ShopImageService] 현재 샵의 기존 이미지 수: " + shopImages.size());

        Set<Long> imageUrlSet=new HashSet<>(imageUrls != null ? imageUrls : new ArrayList<>());
        log.info("DEBUG: [ShopImageService] 유지할 이미지 ID Set: " + imageUrlSet);

        // 1. 기존 이미지 삭제 로직 (유지할 목록에 없는 이미지는 삭제)
        List<ShopImage> imagesToDelete = new ArrayList<>();
        for (ShopImage shopImage : shopImages) {
            if (shopImage.getShopImageId() != null && !imageUrlSet.contains(shopImage.getShopImageId())) {
                imagesToDelete.add(shopImage);
                System.out.println("DEBUG: [ShopImageService] 삭제할 이미지 발견 (ID: " + shopImage.getShopImageId() + ", URL: " + shopImage.getShopImageUrl() + ")");
            }
        }
        for (ShopImage img : imagesToDelete) {
            fileStorageService.deleteFile(img.getShopImageUrl()); // 물리 파일 삭제
            shopImageRepository.delete(img); // DB에서 이미지 레코드 삭제
            System.out.println("DEBUG: [ShopImageService] 이미지 삭제 완료 (ID: " + img.getShopImageId() + ")");
        }


        // 2. 기존 썸네일 플래그 해제 로직 (모든 이미지의 isThumbnail을 false로 초기화)
        // 남아있는 이미지 중에서 isThumbnail이 true인 것을 찾아 false로 변경
        List<ShopImage> remainImagesAfterDeletion = shopImageRepository.findByShop(shop); // 삭제 후 남아있는 이미지 다시 조회 (정확성을 위해)
        System.out.println("DEBUG: [ShopImageService] 이미지 삭제 후 남아있는 이미지 수: " + remainImagesAfterDeletion.size());

        for (ShopImage shopImage : remainImagesAfterDeletion) {
            if (shopImage.getIsThumbnail()) {
                shopImage.deleteThumbnail(); // isThumbnail을 false로 설정하는 메서드
                shopImageRepository.save(shopImage);
                System.out.println("DEBUG: [ShopImageService] 기존 썸네일 플래그 해제 완료 (ID: " + shopImage.getShopImageId() + ", URL: " + shopImage.getShopImageUrl() + ")");
            }
        }


        Map<String, String> saveNewFileUrl = new HashMap<>(); // 새로 저장된 파일의 원본 이름과 URL 매핑
        List<ShopImageDTO> savedShopImageDTOs = new ArrayList<>(); // 새로 저장된 이미지의 DTO 목록 (응답용)

        // 3. 새 이미지 파일 업로드 및 DB 저장
        if (newImageFiles != null && !newImageFiles.isEmpty()) {
            System.out.println("DEBUG: [ShopImageService] 새 이미지 파일 처리 시작...");
            for (MultipartFile file : newImageFiles) {
                String originalFilename = file.getOriginalFilename();
                String url = saveFileAndGetUrl(file); // 실제 파일 저장 및 URL 반환
                saveNewFileUrl.put(originalFilename, url); // 원본 파일명 -> 저장된 URL 맵핑

                ShopImage newShopImage = ShopImage.builder()
                        .shop(shop)
                        .shopImageUrl(url)
                        .isThumbnail(false) // 일단 썸네일 아님으로 설정
                        .build();

                shopImageRepository.save(newShopImage);
                savedShopImageDTOs.add(ShopImageDTO.builder()
                        .shopImageId(newShopImage.getShopImageId())
                        .shopImageUrl(url)
                        .isThumbnail(false)
                        .build());
                System.out.println("DEBUG: [ShopImageService] 새 이미지 저장 완료 (Original: " + originalFilename + ", URL: " + url + ", ID: " + newShopImage.getShopImageId() + ")");
            }
            System.out.println("DEBUG: [ShopImageService] 새 이미지 파일 처리 완료. 총 " + savedShopImageDTOs.size() + "개 저장.");
        } else {
            System.out.println("DEBUG: [ShopImageService] 새로 업로드될 파일이 없습니다.");
        }


        String finalThumbnailUrl = null; // 최종적으로 샵의 썸네일로 설정될 URL

        // 4. 새로운 썸네일 지정 로직 (우선순위: 기존 ID > 새 파일 명시 > 첫 번째 새 파일 > 기존 썸네일)

        // A. 프론트에서 '기존 이미지' 중 썸네일로 지정할 ID를 넘겨준 경우
        if (thumbnailFileId != null) {
            System.out.println("DEBUG: [ShopImageService] 썸네일 지정 시도: 기존 이미지 ID(" + thumbnailFileId + ")로 지정.");
            finalThumbnailUrl = shopImageRepository.findById(thumbnailFileId)
                    .map(shopImage -> {
                        shopImage.changeThumbnail(); // isThumbnail을 true로 설정하는 메서드
                        shopImageRepository.save(shopImage);
                        System.out.println("DEBUG: [ShopImageService] 기존 이미지 ID로 썸네일 설정 완료 (ID: " + shopImage.getShopImageId() + ", URL: " + shopImage.getShopImageUrl() + ")");
                        return shopImage.getShopImageUrl();
                    }).orElseGet(() -> {
                        System.out.println("DEBUG: [ShopImageService] 지정된 기존 이미지 ID(" + thumbnailFileId + ")를 찾을 수 없습니다.");
                        return null;
                    });
        }
        // B. 프론트에서 '새로 업로드된 파일'의 원본 이름 (또는 식별자)을 썸네일로 명시한 경우
        else if (thumbnailUrl != null && !thumbnailUrl.isEmpty()) {
            System.out.println("DEBUG: [ShopImageService] 썸네일 지정 시도: DTO의 thumbnailUrlFromDTO(" + thumbnailUrl + ")로 지정.");
            String urlOfNewFile = saveNewFileUrl.get(thumbnailUrl); // 새로 저장된 URL 맵에서 찾아봄
            if (urlOfNewFile != null) {
                System.out.println("DEBUG: [ShopImageService] DTO의 썸네일 파일 이름에 해당하는 저장된 URL: " + urlOfNewFile);
                // 이 URL을 가진 ShopImage를 찾아서 isThumbnail을 true로 설정
                finalThumbnailUrl = shopImageRepository.findByShop(shop).stream()
                        .filter(shopImage -> shopImage.getShopImageUrl().equals(urlOfNewFile))
                        .findFirst()
                        .map(shopImage -> {
                            shopImage.changeThumbnail();
                            shopImageRepository.save(shopImage);
                            System.out.println("DEBUG: [ShopImageService] DTO의 thumbnailUrlFromDTO로 썸네일 설정 완료 (ID: " + shopImage.getShopImageId() + ", URL: " + shopImage.getShopImageUrl() + ")");
                            return shopImage.getShopImageUrl();
                        }).orElseGet(() -> {
                            System.out.println("DEBUG: [ShopImageService] DTO의 thumbnailUrlFromDTO에 해당하는 ShopImage를 찾을 수 없습니다.");
                            return null;
                        });
            } else {
                System.out.println("DEBUG: [ShopImageService] DTO의 thumbnailUrlFromDTO(" + thumbnailUrl + ")에 해당하는 저장된 파일 URL을 찾을 수 없습니다. 아마도 새로 업로드된 파일이 아니거나 원본 이름이 일치하지 않습니다.");
            }
        }
        // C. 썸네일이 명시적으로 지정되지 않았고, '새로 업로드된 파일'이 있다면 첫 번째 파일을 썸네일로 자동 지정
        else if (newImageFiles != null && !newImageFiles.isEmpty() && finalThumbnailUrl == null) {
            System.out.println("DEBUG: [ShopImageService] 썸네일 지정 시도: 명시된 썸네일 없음. 새로 업로드된 첫 번째 파일을 썸네일로 자동 지정.");
            if (!savedShopImageDTOs.isEmpty()) {
                ShopImageDTO firstNewImageDto = savedShopImageDTOs.get(0);
                finalThumbnailUrl = shopImageRepository.findById(firstNewImageDto.getShopImageId())
                        .map(shopImage -> {
                            shopImage.changeThumbnail();
                            shopImageRepository.save(shopImage);
                            System.out.println("DEBUG: [ShopImageService] 새로 업로드된 첫 번째 이미지로 썸네일 설정 완료 (ID: " + shopImage.getShopImageId() + ", URL: " + shopImage.getShopImageUrl() + ")");
                            return shopImage.getShopImageUrl();
                        }).orElseGet(() -> {
                            System.out.println("DEBUG: [ShopImageService] 새로 업로드된 첫 번째 이미지 ID(" + firstNewImageDto.getShopImageId() + ")를 찾을 수 없습니다.");
                            return null;
                        });
            } else {
                System.out.println("DEBUG: [ShopImageService] 새로 업로드된 파일은 있지만 savedShopImageDTOs가 비어있습니다. (논리적 오류 가능성)");
            }
        }
        // D. 위에 어떤 썸네일도 지정되지 않고, 기존에 썸네일이 있었다면 그 썸네일을 유지하고 반환
        // (여기서는 ShopImage 엔티티의 isThumbnail 플래그를 변경하지 않고, 단순히 Shop 엔티티의 thumbnailImageUrl을 가져와 반환합니다.)
        if (finalThumbnailUrl == null && shop.getThumbnailImageUrl() != null) {
            finalThumbnailUrl = shop.getThumbnailImageUrl();
            System.out.println("DEBUG: [ShopImageService] 명시된 썸네일 없음. 기존 샵의 썸네일 URL을 유지합니다: " + finalThumbnailUrl);
        } else if (finalThumbnailUrl == null) {
            System.out.println("DEBUG: [ShopImageService] 최종적으로 설정된 썸네일 URL이 없습니다. 썸네일이 null로 반환됩니다.");
        }


        System.out.println("DEBUG: [ShopImageService] 최종 반환될 썸네일 URL: " + finalThumbnailUrl);
        System.out.println("DEBUG: [ShopImageService] updateShopImages 메서드 종료.");

        return ImageResponseDTO.builder()
                .shopImageDTOS(savedShopImageDTOs)
                .thumbnailUrl(finalThumbnailUrl) // 최종 썸네일 URL 반환
                .build();


    }

    @Override
    public String saveFileAndGetUrl(MultipartFile file){
        return fileStorageService.storeFile(file, "images/shopImages/");
    }


    @Override
    @Transactional(readOnly = true)
    public List<ShopImageDTO> findShop(Shop shop, Long shopId){
        return shopImageRepository.findShopImages(shopId);
    }
}