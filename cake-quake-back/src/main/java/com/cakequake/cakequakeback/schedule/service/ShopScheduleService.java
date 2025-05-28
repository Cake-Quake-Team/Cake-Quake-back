package com.cakequake.cakequakeback.schedule.service;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.schedule.dto.ShopScheduleResponseDTO;

public interface ShopScheduleService {
    ShopScheduleResponseDTO createShopSchedule(CakeOrder order, Long shopId);
}
