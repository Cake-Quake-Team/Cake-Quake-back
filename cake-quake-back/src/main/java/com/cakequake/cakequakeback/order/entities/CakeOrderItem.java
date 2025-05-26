package com.cakequake.cakequakeback.order.entities;

import com.cakequake.cakequakeback.cart.entities.Cart;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cake_order_item")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CakeOrderItem {

    /** 주문 아이템 ID (PK) */
    @Id
    @GeneratedValue
    @SequenceGenerator(
            name = "order_item_seq_gen",
            sequenceName = "order_item_seq",
            allocationSize = 50
    )
    @Column(name = "orderItemId")
    private Long orderItemId;

    /** 장바구니로부터 바로 주문했을 때 참조할 Cart */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cartId",
            foreignKey = @ForeignKey(name = "fk_cake_order_item_cart")
    )
    private Cart cartId;

    /** 주문 헤더(주문ID) 참조 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orderId",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_cake_order_item_cake_order")
    )
    private CakeOrder cakeOrder;

    /** 케이크 상품 ID */
    @Column(name="cakeId", nullable = false)
    private Long cakeId;

    /** 커스텀 오더 시트 ID (선택 사용) */
    @Column(name = "cakeCustomOrderSheetId")
    private Long cakeCustomOrderSheetId;

    /** 추천 아이디 (recID) */
    @Column(name = "recID")
    private Long recId;

    /** 옵션1: 시트 형태(ID) */
    @Column(name = "sheetId", nullable = false)
    private Long sheetId;

    /** 옵션2: 외부 크림(ID) */
    @Column(name = "creamId", nullable = false)
    private Long creamId;

    /** 옵션3: 기타(ID) */
    @Column(name = "extraId", nullable = false)
    private Long extraId;

    /** 수량 */
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /** 단가 (상품 가격) */
    @Column(name = "price", nullable = false)
    private BigDecimal price;

    /** 커스터마이징 여부
     * null값 허용 true
     * 기본값 false
     */
    @Column(name = "isCustom", nullable = true, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isCustom = Boolean.FALSE;

//    @Column(name = "regDate", nullable = false)
//    private LocalDateTime regDate;

//    @Column(name = "modDate", nullable = false)
//    private LocalDateTime modDate;

}
