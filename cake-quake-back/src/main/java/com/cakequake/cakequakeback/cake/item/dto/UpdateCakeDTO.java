package com.cakequake.cakequakeback.cake.item.dto;

import com.cakequake.cakequakeback.cake.item.CakeCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
// 케이크 상품 수정 요청 DTO
public class UpdateCakeDTO {
    @NotNull(message = "케이크 이름은 필수입니다.")
    private String cname;

    @NotNull(message = "가격은 0원보다 커야 합니다.")
    @Min(value = 1, message = "가격은 0원보다 커야 합니다.")
    private Integer price;

    private String description;

    @NotNull(message = "카테고리 선택은 필수입니다.")
    private CakeCategory category;

    private String thumbnailImageUrl;

    @NotNull(message = "품절여부 체크는 필수입니다.")
    private Boolean isOnsale;   // 품절 : TRUE, 판매 중 : FALSE
}
