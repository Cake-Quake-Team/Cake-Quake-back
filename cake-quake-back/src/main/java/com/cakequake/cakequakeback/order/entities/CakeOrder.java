package com.cakequake.cakequakeback.order.entities;


import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/** 주문 헤더 정보 (cake_order 테이블) */

@Entity
@Table(name = "cake_order")
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CakeOrder extends BaseEntity {

    // 주문 ID (PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "orderId")
    private Long orderId;

    // 주문자 (회원) 필요
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false, foreignKey = @ForeignKey(name = "fk_order_member"))
    private Member member;

    //주문 번호
    @Column(name = "orderNumber", nullable = false, unique = true)
    private String orderNumber;

    //주문한 케이크 상품
    @OneToMany(mappedBy = "orderItemId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CakeOrderItem> cakeOrderItems;

    //주문할 때 구매자가 적은 글
    @Column(name = "orderNote", length = 255)
    private String orderNote;

    //주문 상품 총 개수
    @Column(name = "totalNumber", nullable = false)
    private Integer totalNumber;

    //주문 총 가격
    @Column(name = "orderTotalPrice", nullable = false)
    private Integer orderTotalPrice;

    //픽업 날짜
    @Column(name = "pickupDate", nullable = false)
    private LocalDate pickupDate;

    //픽업 시간
    @Column(name = "pickupTime", nullable = false)
    private LocalTime pickupTime;

    /** 주문 상태 : 주문확인중, 주문확정, 주문취소, 노쇼, 픽업완료 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

}
