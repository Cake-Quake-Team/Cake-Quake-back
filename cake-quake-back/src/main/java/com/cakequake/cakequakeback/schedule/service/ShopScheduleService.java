package com.cakequake.cakequakeback.schedule.service;
import com.cakequake.cakequakeback.shop.entities.Shop;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ShopScheduleService {

   List<LocalTime> getPossiblePickupTime(Long shopId);
    List<LocalTime> getAvailablePickupTimes(Long shopId, LocalDate date);
    List<Shop> getAvailableShops(LocalDate date, LocalTime time);
}
