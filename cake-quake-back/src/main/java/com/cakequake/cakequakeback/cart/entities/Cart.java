package com.cakequake.cakequakeback.cart.entities;

import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "cart")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

    /*장바구니 ID(PK)*/
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    /*장바구니에 담겨있는 상품ID*/
    //Cart엔티티가 여러 개의 CakeItem(담긴 상품)을 가질 수 있으므로, 리스트 필드에 @OneToMany를 선언함
    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "cart")
    private List<CartItem> cartItem;

    /*장바구니 상품들 합친 총 가격*/
    @Column(name = "cartTotalPrice",nullable = false)
    private Integer cartTotalPrice;

}