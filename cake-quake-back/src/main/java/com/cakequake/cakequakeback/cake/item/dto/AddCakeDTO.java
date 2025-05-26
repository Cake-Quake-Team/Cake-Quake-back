package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
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
    @NotNull(message = "케이크 이름은 필수입니다.")
    private String cname;

    @NotNull(message = "가격은 0원보다 커야 합니다.")
    @Min(value = 1, message = "가격은 0원보다 커야 합니다.")
    private Integer price;

    @NotNull(message = "카테고리 선택은 필수입니다.")
    private CakeCategory category;

    private String description;

    private String thumbnailImageUrl;   // 대표 이미지

    private List<String> imageUrls;    // 이미지 URL 목록
}
