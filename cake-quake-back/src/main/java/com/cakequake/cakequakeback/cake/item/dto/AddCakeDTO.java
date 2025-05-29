package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
// 케이크 상품 등록 요청 DTO
public class AddCakeDTO {

    private String cname;
    private Integer price;
    private CakeCategory category;
    private String description;
    private String thumbnailImageUrl;   // 대표 이미지
    private List<String> imageUrls;    // 이미지 URL 목록
}
