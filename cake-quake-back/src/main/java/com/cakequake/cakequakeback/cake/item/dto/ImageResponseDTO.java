package com.cakequake.cakequakeback.cake.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
// 이미지, 썸네일 Url 응답
public class ImageResponseDTO {

    private List<ImageDTO> imageDTOs;
    private String thumbnailUrl;
}

