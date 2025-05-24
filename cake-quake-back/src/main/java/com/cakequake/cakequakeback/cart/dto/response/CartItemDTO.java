package com.cakequake.cakequakeback.cart.dto.response;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CartItemDTO {

    //케이크 상품ID
    private Long cakeId;

    //상품 수량
    private Integer productCnt;

    //기본 가격
//    private BigDecimal basePrice;

    //선택 옵션 상세 리스트
//    private List<OptionDetailDTO> options;

    //개별 아이템 총 가격
    //itemTotalPrice = (기본 상품 가격 × 수량) + (선택된 모든 옵션의 추가 금액 합)
//    private BigDecimal itemTotalPrice;

    @Builder
    public CartItemDTO(Long cakeId, Integer productCnt,
                       BigDecimal basePrice, List<OptionDetailDTO> options,
                       BigDecimal itemTotalPrice) {
        this.cakeId = cakeId;
        this.productCnt = productCnt;
//        this.basePrice = basePrice;
//        this.options = options;
//        this.itemTotalPrice = itemTotalPrice;
    }
}