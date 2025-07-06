package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.item.entities.CakeCategory;
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
    private String thumbnailImageUrl;
    private List<ImageDTO> imageUrls;    // imageId=null, isThumbnail 플래그만 사용
    private MappingRequestDTO mappingRequestDTO;    // 매핑할 옵션 목록
}
