package com.cakequake.cakequakeback.schedule.service;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.schedule.dto.ShopOperatingHoursDTO;
import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
import com.cakequake.cakequakeback.schedule.dto.ShopScheduleDTO;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ShopScheduleService {

    List<Integer> parseCloseDays(String closeDaysString);
   List<LocalTime> getPossiblePickupTime(Long shopId);
    List<LocalTime> getAvailablePickupTimes(Long shopId, LocalDate date);
    Page<ShopScheduleDTO> getAvailableShops(LocalDate date, LocalTime time, boolean checkSlots,int page, int size);


    //주문 상태 변경 시 슬롯 조정
    void adjustScheduleSlotsForOrderStatusChange(CakeOrder order, OrderStatus newStatus);

    ShopOperatingHoursDTO getShopOperatingHours(Long shopId, LocalDate date);

    List<String> getOccupiedTimeSlots(Long shopId, LocalDate date);

    void decreaseSlotsForOrderCreation(Long shopId, LocalDate pickupDate, LocalTime pickupTime);
}
