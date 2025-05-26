package com.cakequake.cakequakeback.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//장바구니에 새 아이템을 추가할 때 사용하는 요청 DTO

@Getter
@NoArgsConstructor
public class AddCartRequestDTO {

    //상품 담을 매장ID
    @NotNull(message = "shopId는 필수입니다.")
    private Long shopId;

    //장바구니에 담을 상품ID
    @NotNull(message = "cakeId는 필수입니다.")
    private Long cakeId;

    //담은 상품 수량(1~99)
    @Min(value = 1, message = "productCnt는 최소 1개 이상이어야 합니다.")
    private Integer productCnt;

    //선택한 옵션 목록
//    @NotNull
//    private List<CustomOptionDTO> options;

    @Builder
    public AddCartRequestDTO( Long shopId, Long cakeId,
                          Integer productCnt, List<CustomOptionDTO> options) {
        this.shopId = shopId;
        this.cakeId = cakeId;
        this.productCnt = productCnt;
//        this.options = options;
    }
}
