package com.cakequake.cakequakeback.cart.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 장바구니 추가/수정 요청
 * 어떤 옵션 타입에 어떤 옵션을 선택했는지 나타내는 DTO
 */
@Getter
@NoArgsConstructor
public class CustomOptionDTO {

    //옵션 그룹(모양,시트,단,크기,시트 맛,속크림,겉크림,기타)
    @NotNull(message = "optionTypeId는 필수입니다.")
//    private Long optionTypeId;

    //옵션 항목( (ex)1호, 2호 / 생크림,초코크림) ID
    @NotNull(message = "optionId는 필수입니다.")
//    private Long optionId;

    @Builder
    public CustomOptionDTO(Long optionTypeId, Long optionId) {
//        this.optionTypeId = optionTypeId;
//        this.optionId = optionId;
    }
}
