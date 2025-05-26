package com.cakequake.cakequakeback.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//기존 장바구니 아이템을 수정(수량/옵션 변경)할 때 사용하는 요청 DTO

@Getter
@NoArgsConstructor
public class UpdateCartRequestDTO {

    //수정 대상 장바구니
    @NotNull(message = "cartId는 필수입니다.")
    private Long cartId;

    //변경할 상품 수량
    @Min(value = 1, message = "productCnt는 최소 1개 이상이어야 합니다.")
    private Integer productCnt;

    // 옵션 업데이트 시 전체 옵션 리스트를 다시 전달
//    @NotNull
//    private List<CustomOptionDTO> options;

    @Builder
    public UpdateCartRequestDTO(Long cartId, Integer productCnt, List<CustomOptionDTO> options) {
        this.cartId = cartId;
        this.productCnt = productCnt;
        //this.options = options;
    }
}
