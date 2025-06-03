package com.cakequake.cakequakeback.order.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeOptionMapping;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cake_order_item_option")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class CakeOrderItemOption extends BaseEntity {
    // PK
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderItemOptionId")
    private Long orderItemOptionId;

    // 이 옵션이 적용된 주문 아이템 (CakeOrderItem 참조)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orderItemId",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_option_cake_order_item")
    )
    private CakeOrderItem cakeOrderItem;

    // 선택된 케이크-옵션 매핑 (CakeOptionMapping 참조)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "mappingId",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_item_option_cake_option_mapping")
    )
    private CakeOptionMapping cakeOptionMapping;

    // 해당 옵션을 몇 개 적용했는지 (예: 토핑 2개)
    @Column(nullable = false)
    private Integer optionCnt;
}
