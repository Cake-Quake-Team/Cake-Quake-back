package com.cakequake.cakequakeback.cart.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;

/** CartResponse에서 각 옵션의 상세 정보 */

@Getter
public class OptionDetailDTO {

    //옵션 그룹ID
//    private Long optionTypeId;

    //옵션 그룹 이름
//    private String optionTypeName;

    //옵션 아이템 ID
//    private Long optionId;

    //옵션 아이템 이름(1호,바닐라 시트)
//    private String optionName;

    //해당 옵션 선택 시 추가되는 가격
//    private BigDecimal extraPrice;  //필요할 거 같아서 넣긴 함

    @Builder
    public OptionDetailDTO(Long optionTypeId, String optionTypeName,
                           Long optionId, String optionName,
                           BigDecimal extraPrice) {
//        this.optionTypeId = optionTypeId;
//        this.optionTypeName = optionTypeName;
//        this.optionId = optionId;
//        this.optionName = optionName;
//        this.extraPrice = extraPrice;
    }
}
