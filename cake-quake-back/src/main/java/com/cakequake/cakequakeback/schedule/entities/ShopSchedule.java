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

    /**
     * 예약 생성/확정 시 슬롯 감소
     */
    public void decreaseAvailableSlots(int count) {
        if (count <= 0) throw new IllegalArgumentException("감소 수량은 0보다 커야 합니다.");

        if (this.availableSlots >= count) {
            this.availableSlots -= count;
            updateStatusBasedOnSlots();
        } else {
            throw new IllegalArgumentException("요청한 슬롯 수가 남은 슬롯보다 많습니다.");
        }
    }

    /**
     * 예약 취소 시 슬롯 증가
     */
    public void increaseAvailableSlots(int count) {
        if (count <= 0) throw new IllegalArgumentException("증가 수량은 0보다 커야 합니다.");

        this.availableSlots = Math.min(this.availableSlots + count, this.maxSlots);
        updateStatusBasedOnSlots();
    }

    /**
     * 슬롯 수에 따라 예약 상태 자동 변경
     */
    private void updateStatusBasedOnSlots() {
        if (this.availableSlots == 0) {
            this.status = ReservationStatus.CLOSED;
        } else {
            this.status = ReservationStatus.AVAILABLE;
        }
    }

    /**
     * 예약 상태 강제 변경 (필요 시)
     */
    public void changeStatus(ReservationStatus status) {
        this.status = status;
    }

    /**
     * 현재 예약 가능 여부 (편의 메서드)
     */
    public boolean isReservable() {
        return this.status == ReservationStatus.AVAILABLE && this.availableSlots > 0;
    }

}
