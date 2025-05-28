package com.cakequake.cakequakeback.schedule.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@Builder

public class ShopScheduleResponseDTO {

    private Long scheduleId;
    private Long shopId;
    private LocalDateTime scheduleDateTime;
    private boolean reserved;

    public ShopScheduleResponseDTO(Long scheduleId, Long shopId, LocalDateTime scheduleDateTime, boolean reserved) {
        this.scheduleId = scheduleId;
        this.shopId = shopId;
        this.scheduleDateTime = scheduleDateTime;
        this.reserved = reserved;
    }
}



