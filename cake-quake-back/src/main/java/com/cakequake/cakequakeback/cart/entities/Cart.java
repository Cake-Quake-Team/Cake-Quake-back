package com.cakequake.cakequakeback.cart.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Cart extends BaseEntity {

    /*장바구니 ID(PK)*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartId;

    /*회원ID*/
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "userId",
            nullable=false,
            unique = true
    )
    private Member member;

    /*CakeOrder와 1:1 연관관계*/
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, foreignKey = @ForeignKey(name = "fk_cart_order"))
    private CakeOrder order;

    /*장바구니 상품들 합친 총 가격*/
    @Column(nullable = false)
    private Integer cartTotalPrice;

}