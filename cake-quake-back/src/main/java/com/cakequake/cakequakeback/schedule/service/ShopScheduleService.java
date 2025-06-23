package com.cakequake.cakequakeback.schedule.service;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
import com.cakequake.cakequakeback.shop.entities.Shop;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ShopScheduleService {

   List<LocalTime> getPossiblePickupTime(Long shopId);
    List<LocalTime> getAvailablePickupTimes(Long shopId, LocalDate date);
    List<Shop> getAvailableShopsByDate(LocalDate date);

    //주문 생성 시 슬롯 감소 및 스케줄 생성
    ShopSchedule decreaseSlotsForOrderCreation(CakeOrder order);

    //주문 상태 변경 시 슬롯 조정
    void adjustScheduleSlotsForOrderStatusChange(CakeOrder order, OrderStatus oldStatus, OrderStatus newStatus);
}
