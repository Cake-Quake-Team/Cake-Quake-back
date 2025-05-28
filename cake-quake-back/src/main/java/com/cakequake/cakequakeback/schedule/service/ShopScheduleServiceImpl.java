package com.cakequake.cakequakeback.schedule.service;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.schedule.dto.ShopScheduleResponseDTO;
import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
import com.cakequake.cakequakeback.schedule.repo.ShopScheduleRepository;
import com.cakequake.cakequakeback.shop.entities.Shop;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ShopScheduleServiceImpl implements ShopScheduleService {
    private final ShopRepository shopRepository;
    private final ShopScheduleRepository shopScheduleRepository;

    //스케줄 생성
    @Transactional
    public ShopScheduleResponseDTO createShopSchedule(CakeOrder order, Long shopId) {
        // 1. 날짜 + 시간 합치기
        LocalDateTime scheduleDateTime = LocalDateTime.of(
                order.getPickupDate().toLocalDate(),
                order.getPickupTime().toLocalTime()
        );

        // 2. 해당 스케줄이 이미 예약되었는지 확인
        Optional<ShopSchedule> existingSchedule = shopScheduleRepository
                .findShopSchedule(shopId, scheduleDateTime);

        if (existingSchedule.isPresent() && existingSchedule.get().isReserved()) {
            throw new IllegalStateException("이미 예약된 시간입니다.");
        }

        ShopSchedule schedule = existingSchedule.orElseGet(() -> {
            Shop shop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new EntityNotFoundException("Shop not found"));

            return new ShopSchedule(shop, scheduleDateTime, true, order);
        });

        shopScheduleRepository.save(schedule);
           return new ShopScheduleResponseDTO(
                schedule.getScheduleId(),
                schedule.getShop().getShopId(),
                schedule.getScheduleDateTime(),
                schedule.isReserved()
        );
    }
}
