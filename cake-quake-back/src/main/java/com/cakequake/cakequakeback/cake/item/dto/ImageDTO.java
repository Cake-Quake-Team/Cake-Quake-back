package com.cakequake.cakequakeback.cake.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 요청용 DTO
public class ImageDTO {
    private Long imageId;       // DB에 저장된 이미지일 경우만 존재
    private String imageUrl;    // 이미지 경로 (신규 추가 시 필요)
    private Boolean isThumbnail; // 썸네일 여부
}



