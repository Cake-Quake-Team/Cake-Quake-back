package com.cakequake.cakequakeback.schedule.entities;

import com.cakequake.cakequakeback.shop.entities.Shop;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Builder
@Entity
@Table( name = "shop_schedule")
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class ShopSchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "schedule_seq_gen")
    @SequenceGenerator(
            name ="schedule_seq_gen",
            sequenceName = "schedule_seq",
            initialValue = 1,
            allocationSize = 50
    )
    private Long scheduleId;

    @ManyToOne(fetch =FetchType.LAZY)
    @JoinColumn(name="shopId",nullable = false)
    private Shop shop;

    @Column(nullable = false)
    private LocalDate availableDate;

    @Column(nullable = false)
    private LocalTime startTime;

    @Column
    private String description;



}
