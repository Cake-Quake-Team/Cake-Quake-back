package com.cakequake.cakequakeback.schedule.controller;

import com.cakequake.cakequakeback.schedule.dto.ShopOperatingHoursDTO;
import com.cakequake.cakequakeback.schedule.service.ShopScheduleService;
import com.cakequake.cakequakeback.schedule.dto.ShopScheduleDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 🔹 3. 특정 매장의 특정 날짜에 대한 운영 시간 정보를 조회합니다.
     * 프론트엔드의 `getShopOperatingHours` API 호출에 대응합니다.
     */
    @GetMapping("/shops/{shopId}/operating-hours")
    public ResponseEntity<ShopOperatingHoursDTO> getShopOperatingHours(
            @PathVariable Long shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        ShopOperatingHoursDTO operatingHours = shopScheduleService.getShopOperatingHours(shopId, date);
        return ResponseEntity.ok(operatingHours);
    }

    /**
     * 🔹 4. 특정 매장, 특정 날짜에 예약이 가득 찬 (더 이상 예약 불가능한) 시간 목록을 HH:MM 문자열 형식으로 조회합니다.
     * 프론트엔드의 `getOccupiedTimeSlots` API 호출에 대응합니다.
     */
    @GetMapping("/shops/{shopId}/occupied-time-slots")
    public ResponseEntity<List<String>> getOccupiedTimeSlots(
            @PathVariable Long shopId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<String> occupiedSlots = shopScheduleService.getOccupiedTimeSlots(shopId, date);
        return ResponseEntity.ok(occupiedSlots);
    }

}