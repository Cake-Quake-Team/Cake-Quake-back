package com.cakequake.cakequakeback.schedule.controller;

import com.cakequake.cakequakeback.schedule.service.ShopScheduleService;
import com.cakequake.cakequakeback.schedule.dto.ShopScheduleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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


    /**
     * ✅ 1. 특정 날짜에 예약 가능한 매장 목록 조회 (슬롯 수 고려 X → 단순 오픈 상태 및 휴무일만 체크)
     */
    @GetMapping("/available-shops-by-date")
    public ResponseEntity<List<ShopScheduleDTO>> getAvailableShopsByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam(defaultValue = "true") boolean checkSlots
    ) {
        List<ShopScheduleDTO> availableShops = shopScheduleService.getAvailableShops(date, time, checkSlots);
        return ResponseEntity.ok(availableShops);
    }

    /**
     * ✅ 2. 특정 매장 + 날짜의 예약 가능한 시간 목록 조회 (슬롯 수 고려 O)
     */
    @GetMapping("/available-times")
    public ResponseEntity<List<LocalTime>> getAvailableTimesForShop(
            @RequestParam Long shopId,
            @RequestParam LocalDate date) {

        List<LocalTime> availableTimes = shopScheduleService.getAvailablePickupTimes(shopId, date);
        return ResponseEntity.ok(availableTimes);
    }


}