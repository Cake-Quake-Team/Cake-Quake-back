package com.cakequake.cakequakeback.order.entities;


import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.common.entities.BaseEntity;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.shop.entities.Shop;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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

    // 매장(Shop)과 연결 (ManyToOne)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shopId", nullable = false, foreignKey = @ForeignKey(name = "fk_order_shop"))
    private Shop shop;

    //주문 번호
    @Column(name = "orderNumber", nullable = false, unique = true)
    private String orderNumber;

    //주문할 때 구매자가 적은 글
    @Column(name = "orderNote", length = 255)
    private String orderNote;

    //주문 상품 총 개수
    @Column(name = "totalNumber", nullable = false)
    private Integer totalNumber;

    //주문 총 가격
    @Column(name = "orderTotalPrice", nullable = false)
    private Integer orderTotalPrice;

    // 새로 추가함
    // 포인트 할인 금액 (사용된 포인트)
    @Column(name = "discountAmount", nullable = false)
    private Integer discountAmount;

    // 최종 결제 금액 (할인 적용 후)
    // 새로 추가함
    @Column(name = "finalPaymentAmount", nullable = false)
    private Integer finalPaymentAmount;

    //픽업 날짜
    @Column(name = "pickupDate", nullable = false)
    private LocalDate pickupDate;

    //픽업 시간
    @Column(name = "pickupTime", nullable = false)
    private LocalTime pickupTime;

    /** 주문 상태 : 주문확인중, 주문확정, 주문취소, 노쇼, 픽업완료 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private OrderStatus status;

    // 주문 총 금액 설정
    public void applyOrderTotalPrice(long price) {
        if (price < 0) throw new IllegalArgumentException("총액은 0 이상이어야 합니다.");
        this.orderTotalPrice = (int) price;
    }

    // 총 상품 수량 설정
    public void applyTotalNumber(int totalCount) {
        if (totalCount < 1) throw new IllegalArgumentException("총 수량은 1개 이상이어야 합니다.");
        this.totalNumber = totalCount;
    }

    public void updateStatus(OrderStatus newStatus) {
        // 필요하다면 여기서 상태 전환에 대한 추가 검증 로직을 넣을 수 있습니다.
        // 예를 들어, 이미 취소된 주문은 다시 상태 변경 불가 등
        this.status = newStatus;
    }
}
