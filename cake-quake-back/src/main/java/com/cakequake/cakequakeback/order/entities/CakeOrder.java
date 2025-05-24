package com.cakequake.cakequakeback.order.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * 주문 헤더 정보 (cake_order 테이블)
 */
@Entity
@Table(name = "cake_order")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CakeOrder {

    /** 주문 ID (PK) */
    @Id
    @GeneratedValue
    @SequenceGenerator(
            name = "cake_order_seq_gen",
            sequenceName = "cake_order_seq",
            allocationSize = 50
    )
    @Column(name = "orderId")
    private Long orderId;

    /** 예약(Reservation) FK */
    @Column(name = "reservationId", nullable = false)
    private Long reservationId;

    /** 주문 통합 가격 (totalPrice) */
    @Column(name = "totalPrice", nullable = false)
    private Integer totalPrice;

    /** 주문 상태 예약확인중, 예약확정, 예약취소, 노쇼, 픽업완료 */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    /** 주문 생성 시각 */
//    @Column(name = "orderedAt", nullable = false)
//    private LocalDateTime orderedAt;
//
    /** 마지막 수정 시각 */
//    @Column(name = "modDate", nullable = false)
//    private LocalDateTime modDate;
}
