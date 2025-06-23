package com.cakequake.cakequakeback.schedule.entities;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.shop.entities.Shop;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;




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

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReservationStatus status; //예약 상태

    @Column(nullable = false)
    private Integer maxSlots;

    @Column(nullable = false)
    private Integer availableSlots;

    //예약 생성, 확정 시 -> 슬롯 감소
    public void decreaseAvailableSlots(int count) {
        if (this.availableSlots >= count) {
            this.availableSlots -= count;
            if (this.availableSlots == 0) {
                this.status = ReservationStatus.CLOSED; // ⭐ 변경: ScheduleStatus.FULL -> ReservationStatus.CLOSED (가장 적합한 상태)
            } else if (this.status == ReservationStatus.CLOSED) { // 슬롯이 0에서 1개 이상이 되면 다시 AVAILABLE로 변경
                this.status = ReservationStatus.AVAILABLE;
            }
        } else {
            throw new IllegalArgumentException("요청한 슬롯 수가 남은 슬롯보다 많습니다.");
        }
    }

    //예약 취소 시 -> 슬롯 증가
    public void increaseAvailableSlots(int count) {
        if (this.availableSlots + count <= this.maxSlots) {
            this.availableSlots += count;
            if (this.status == ReservationStatus.CLOSED || this.status == ReservationStatus.CANCELLED) { // 슬롯이 생기면 다시 AVAILABLE 상태로 변경
                this.status = ReservationStatus.AVAILABLE;
            }
        } else {
            this.availableSlots = this.maxSlots;
            this.status = ReservationStatus.AVAILABLE;
        }
    }

    //스케줄 상태 변경
    public void changeStatus(ReservationStatus status) {
        this.status = status;
    }




}
