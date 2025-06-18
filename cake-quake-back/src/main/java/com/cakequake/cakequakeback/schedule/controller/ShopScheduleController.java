package com.cakequake.cakequakeback.schedule.controller;

import com.cakequake.cakequakeback.schedule.service.ShopScheduleService;
import com.cakequake.cakequakeback.shop.entities.Shop;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/schedule")

public class ShopScheduleController{

    private final ShopScheduleService shopScheduleService;

    //특정 매장과 날짜에 대해 예약 가능한 픽업 시간 목록을 조회
    @GetMapping("/available-times")
    public ResponseEntity<List<LocalTime>> getAvailableTimesForShop(
            @RequestParam Long shopId,
            @RequestParam LocalDate date) {
        List<LocalTime> availableTimes = shopScheduleService.getAvailablePickupTimes(shopId, date);
        return ResponseEntity.ok(availableTimes);
    }

    //특정 날짜와 시간에 예약 가능한 매장 목록을 조회
    @GetMapping("/available-shops")
    public ResponseEntity<List<Shop>> getAvailableShopsForTimeSlot(
            @RequestParam LocalDate date,
            @RequestParam LocalTime time) {
        List<Shop> availableShops = shopScheduleService.getAvailableShops(date, time);
        return ResponseEntity.ok(availableShops);
    }


}