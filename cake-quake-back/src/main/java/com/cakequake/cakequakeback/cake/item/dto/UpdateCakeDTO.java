package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
// 케이크 상품 수정 요청 DTO
public class UpdateCakeDTO {

    private String cname;

    private Integer price;

    private String description;

    private CakeCategory category;

    private String thumbnailImageUrl;

    private List<ImageDTO> imageUrls;

    private Boolean isOnsale;   // 품절 : TRUE, 판매 중 : FALSE

    private List<Long> optionItemIds;
}
