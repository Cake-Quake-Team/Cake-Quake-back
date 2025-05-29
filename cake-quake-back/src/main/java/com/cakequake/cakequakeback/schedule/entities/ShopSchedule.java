package com.cakequake.cakequakeback.schedule.entities;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.shop.entities.Shop;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Entity
@Table( name = "shop_schedule")
@Getter
@NoArgsConstructor


public class ShopSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long scheduleId;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="shopId",nullable = false)
    private Shop shop;

    // ✅ 예약 일시를 직접 가짐 (날짜 + 시간)
    @Column(name = "schedule_datetime", nullable = false)
    private LocalDateTime scheduleDateTime;

    @Column(nullable = false)
    private boolean isReserved;

    // Optional: 예약된 주문
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId")
    private CakeOrder cakeOrder;


    public ShopSchedule( Shop shop, LocalDateTime scheduleDateTime, boolean isReserved, CakeOrder cakeOrder) {
        this.shop = shop;
        this.scheduleDateTime = scheduleDateTime;
        this.isReserved = isReserved;
        this.cakeOrder = cakeOrder;
    }
}
