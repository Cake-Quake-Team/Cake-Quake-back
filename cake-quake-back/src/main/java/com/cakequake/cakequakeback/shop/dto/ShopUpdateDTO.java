package com.cakequake.cakequakeback.shop.dto;

import com.cakequake.cakequakeback.cake.item.dto.ImageDTO;
import com.cakequake.cakequakeback.shop.entities.ShopStatus;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString

public class ShopUpdateDTO {

    private String address; //not null
    private String phone;
    private String content; //not null
    private LocalTime openTime;//not
    private LocalTime closeTime; //not
    private String closeDays;
    private String websiteUrl;
    private String instagramUrl;
    private ShopStatus status;
    private String thumbnailImageUrl;
    private List<ShopImageDTO> imageUrls;
    private List<Long> shopImageIdsToDelete; // 삭제할 기존 이미지 ID 목록 (프론트에서 JSON.stringify로 보낸 것을 받을 것)
    private List<ShopImageDTO> existingImageDtos; // 기존 이미지들의 ID와 isThumbnail 상태 (프론트에서 JSON.stringify로 보낸 것을 받을 것)
    private List<MultipartFile> newFiles; // 새로 업로드될 파일들
    private Long thumbnailImageId; // 기존 이미지 중 썸네일로 지정될 ID
    private Integer thumbnailNewFileIndex; // 새로 업로드될 파일 중 썸네일로 지정될 파일의 인덱스 (newFiles 리스트의 인덱스)




}
