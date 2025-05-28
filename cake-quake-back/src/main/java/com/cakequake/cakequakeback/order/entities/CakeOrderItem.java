package com.cakequake.cakequakeback.order.entities;

import com.cakequake.cakequakeback.cake.option.entities.OptionItem;
import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    //주문 타입(직접 주문, 장바구니를 통해서 주문)
    @Enumerated(EnumType.STRING)
    @Column(name = "orderType", nullable = false, length = 10)
    private OrderType orderType;

    // 주문 헤더(주문ID) 참조
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orderId",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cake_order_item_cake_order")
    )
    private CakeOrder cakeOrder;



    // 케이크 상품별 옵션들
    @OneToMany(mappedBy = "optionItemId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OptionItem> optionItems;


}
