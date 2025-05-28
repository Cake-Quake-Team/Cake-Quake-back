package com.cakequake.cakequakeback.order.entities;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;

@Entity
@Table(name = "cake_order_item")
@Getter
@NoArgsConstructor
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
    private CakeItem cakeId;

    // 주문 헤더(주문ID) 참조
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orderId",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cake_order_item_cake_order")
    )
    private CakeOrder orderId;


    // 케이크 상품별 옵션들
    @OneToMany(mappedBy = "optionItemId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionItem> optionItems;

}