package com.cakequake.cakequakeback.order.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "cake_order_item")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class CakeOrderItem extends BaseEntity {

    // 주문 아이템 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderItemId")
    private Long orderItemId;

    // 어떤 케이크인지 참조
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cakeId",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_cake_item")
    )
    private CakeItem cakeItem;

    // 주문 헤더(주문ID) 참조
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orderId",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cake_order_item_cake_order")
    )
    private CakeOrder cakeOrder;

    // 해당 케이크를 주문한 수량
    @Column(nullable = false)
    private Integer quantity;

    // 주문 시점의 단가 (한 개당 가격)
    @Column(name = "unitPrice", nullable = false)
    private Integer unitPrice;

    // 주문 아이템 소계 금액 (unitPrice * quantity)
    @Column(nullable = false)
    private BigDecimal subTotalPrice;
    
}