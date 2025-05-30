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
@AllArgsConstructor
@Builder


public class ShopSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long scheduleId;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="shopId",nullable = false)
    private Shop shop; //가게ID

    @Column(name = "schedule_datetime", nullable = false)
    private LocalDateTime scheduleDateTime; //예약 가능 시간

    @Column
    @Enumerated(EnumType.STRING)
    private ReservationStatus status; //예약 상태




}
