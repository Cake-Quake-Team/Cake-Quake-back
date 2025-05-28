package com.cakequake.cakequakeback.schedule.controller;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.schedule.dto.ShopScheduleRequestDTO;
import com.cakequake.cakequakeback.schedule.dto.ShopScheduleResponseDTO;
import com.cakequake.cakequakeback.schedule.entities.ShopSchedule;
import com.cakequake.cakequakeback.schedule.service.ShopScheduleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/schedule")

public class ShopScheduleController{

    private final ShopScheduleService shopScheduleService;

//    @PostMapping
//    public ResponseEntity<ShopScheduleResponseDTO> createCakeOrder(@RequestBody @Valid ShopScheduleRequestDTO shopScheduleRequestDTO) {
////        CakeOrder order = new CakeOrder(
////                shopScheduleRequestDTO.getScheduleDateTime().toLocalDate(),
////                shopScheduleRequestDTO.getScheduleDateTime().toLocalTime()
////        );
//
//        CakeOrder order = CakeOrder.builder()
//                .pickupDate(shopScheduleRequestDTO.getScheduleDateTime().toLocalDate())
//                .pickupTime(shopScheduleRequestDTO.getScheduleDateTime().toLocalTime())
//                // 필요한 다른 주문 필드들 세팅 가능
//                .build();
//
//        ShopScheduleResponseDTO response = shopScheduleService.createShopSchedule(order,shopScheduleRequestDTO.getShopId());
//        return ResponseEntity.ok(response);
//
//    }
}