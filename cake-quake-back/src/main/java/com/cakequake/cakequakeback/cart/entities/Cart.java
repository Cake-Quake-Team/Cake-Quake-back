package com.cakequake.cakequakeback.cart.entities;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.entities.Shop;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

    /*장바구니 ID(PK)*/
    @Id
    @GeneratedValue
    @SequenceGenerator(
            name = "cart_seq_gen",         // JPA 내부에서 참조하는 시퀀스 생성기 이름
            sequenceName = "cart_seq",      // DB에 생성된 시퀀스 이름
            allocationSize = 50              // 한 번에 시퀀스를 얼마나 가져올지 설정
    )
    @Column(name = "cartId")
    private Long cartId;

    /*회원ID*/
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "uid",
            nullable=false,
            unique = true,
            foreignKey = @ForeignKey(name="fk_cart_uid")
    )
    private Member member;

    /*상품ID*/
    @Column(name="cakeId", nullable=false)
    private Long cakeId;

    /* 매장 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "shopId",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cart_shop")
    )
    private Shop shop;

    /*장바구니에 담은 수량ID*/
    @Min(1)
    @Max(99)
    @Column(nullable=false)
    private Integer productCnt;

    /*장바구니 합계*/
    @Column(nullable = false)
    private Long cnt;

}