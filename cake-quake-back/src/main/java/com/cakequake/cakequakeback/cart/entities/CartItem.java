package com.cakequake.cakequakeback.cart.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.util.List;
import java.util.Map;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class CartItem extends BaseEntity {

    //장바구니 안에 들어있는 상품ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long cartItemId;

    //CartItem은 반드시 하나의 cart에 속해야함으로 ManyToOne 매핑함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cartId",foreignKey = @ForeignKey(name = "fk_cartItem_cart"))
    private Cart cart;

    // CartItem은 하나의 CakeId(상품)만 참조하므로 ManyToOne 매핑함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cakeItem",nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_cake_item"))
    private CakeItem cakeItem;

    @Column(name = "selected_options", columnDefinition = "TEXT") // DB 컬럼명은 selected_options
    private String selectedOptions;

    //장바구니에 담은 수량
    @Min(1)
    @Max(99)
    @Column(name="productCnt", nullable=false)
    private Integer productCnt;

    //장바구니에 담긴 상품 총 가격
    @Column(name = "itemTotalPrice", nullable = false)
    private Long itemTotalPrice;

    public Integer getUnitPrice() { // int 대신 Integer 반환 타입 사용
        // 단가는 CakeItem에서 가져오는 것이 일반적입니다.
        if (this.cakeItem != null) {
            return this.cakeItem.getPrice(); // CakeItem.getPrice()가 Integer를 반환한다고 가정
        }
        // cakeItem이 null이거나 가격 정보가 없다면 0 또는 예외 처리
        return 0; // 또는 throw new IllegalStateException("CakeItem이 연결되어 있지 않습니다.");
    }

    public Integer getQuantity() { // int 대신 Integer 반환 타입 사용
        return this.productCnt;
    }


    // 만약 CartItem에 options 필드가 있다면
    // 없다면 비어있는 Map을 반환하거나, CakeItem에서 가져와야 할 수 있습니다.
    // return Collections.emptyMap();
    @Getter
    @Transient // DB에 매핑되지 않는 임시 필드 또는 실제 매핑된 필드 추가
    private Map<Long, Integer> options; // ⭐ CartItem이 직접 옵션 맵을 가지고 있다면 이 필드 추가 ⭐

}