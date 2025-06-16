package com.cakequake.cakequakeback.cake.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 요청용 DTO
public class ImageDTO {
    private Long imageId;       // DB에 저장된 이미지일 경우만 존재
    private String imageUrl;    // 이미지 경로 (신규 추가 시 필요)
    private Boolean isThumbnail; // 썸네일 여부
    private MultipartFile imageFile;    // 새로 업로드될 파일 객체

    public ImageDTO(Long imageId, String imageUrl, Boolean isThumbnail) {
        this.imageId = imageId;
        this.imageUrl = imageUrl;
        this.isThumbnail = isThumbnail;
    }
}