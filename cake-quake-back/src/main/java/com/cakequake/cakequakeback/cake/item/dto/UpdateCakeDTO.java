package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
// 케이크 상품 수정 요청 DTO
public class UpdateCakeDTO {

    private String cname;

    @Min(value = 1, message = "가격은 0원보다 커야 합니다.")
    private Integer price;

    private String description;

    private CakeCategory category;

    private String thumbnailImageUrl;

    private Boolean isOnsale;   // 품절 : TRUE, 판매 중 : FALSE
}
