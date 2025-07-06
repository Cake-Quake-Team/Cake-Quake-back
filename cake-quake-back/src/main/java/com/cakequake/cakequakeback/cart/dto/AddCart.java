package com.cakequake.cakequakeback.cart.dto;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

//장바구니에 새 아이템을 추가할 때 사용하는 요청 DTO
public class AddCart {

    @Getter
    @NoArgsConstructor
    @Builder
    @AllArgsConstructor
    public static class Request {

        //장바구니에 담을 상품
        @Valid
        @NotNull(message = "상품 ID 필수입니다.")
        private  Long cakeItemId;

        //담은 상품 수량(1~99)
        @Min(value = 1, message = "productCnt는 최소 1개 이상이어야 합니다.")
        @Max(value = 99, message="productCnt는 최소 99개 미만이어야 합니다.")
        private Integer productCnt;

        @Valid
        private List<CartItemOption> cakeOptions;

    }
    @Getter
    @NoArgsConstructor // Lombok을 사용한다면 @NoArgsConstructor, @AllArgsConstructor 추가
    @Builder
    @AllArgsConstructor
    public static class CartItemOption {
        @NotNull(message = "옵션 아이템 ID는 필수입니다.")
        private Long optionItemId; // 선택된 OptionItem의 ID

        // 옵션 이름과 값도 함께 받아야 프론트에서 표시하기 용이합니다.
        // 백엔드에서 OptionItem을 조회하여 이름/값을 가져올 수도 있지만,
        // 클라이언트에서 함께 전달받는 것이 효율적일 수 있습니다.
        private String optionName; // 예: "맛", "사이즈", "레터링 문구"
        private String optionValue; // 예: "초코", "미니", "생일 축하해!"
        private Integer optionPrice; // 옵션 가격 (선택 사항, 필요 시)


        @Min(value = 0, message = "옵션 수량은 0개 이상이어야 합니다.") // 옵션 개수가 0개일 수도 있음 (선택 안함)
        private Integer optionCnt; // 해당 옵션의 수량 (예: 토핑 2개)
    }

    @Getter
    @Builder
    public static class Response {

        private Long cartItemId; // 장바구니에 추가된 아이템의 ID
        private Long cakeItemId;
        private String cname; // (선택적) 상품명
        private Integer productCnt; // 최종 수량
        private Long itemTotalPrice; // 해당 아이템의 총 가격
        private String selectedOptions;

    }

}
