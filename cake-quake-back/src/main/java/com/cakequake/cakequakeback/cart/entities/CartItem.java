package com.cakequake.cakequakeback.cart.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "cart_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem {

    //장바구니 안에 들어있는 상품ID (PK)
    @Id
    @GeneratedValue
    @SequenceGenerator(
            name = "cart_seq_gen",         // JPA 내부에서 참조하는 시퀀스 생성기 이름
            sequenceName = "cart_seq",      // DB에 생성된 시퀀스 이름
            allocationSize = 50              // 한 번에 시퀀스를 얼마나 가져올지 설정
    )
    @Column(name = "cartItemId")
    private long cartItemId;

    //CartItem은 반드시 하나의 cart에 속해야함으로 ManyToOne 매핑함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cartId",foreignKey = @ForeignKey(name = "fk_cartItem_cart"))
    private Cart cart;

    // CartItem은 하나의 CakeId(상품)만 참조하므로 ManyToOne 매핑함
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cakeItem",nullable = false, foreignKey = @ForeignKey(name = "fk_cart_item_cake_item"))
    private CakeItem cakeItem;

    /*장바구니에 담은 수량*/
    @Min(1)
    @Max(99)
    @Column(name="productCnt", nullable=false)
    private Integer productCnt;

    /*장바구니에 담긴 상품 총 가격*/
    @Column(name = "itemTotalPrice", nullable = false)
    private Long itemTotalPrice;

}
