package com.cakequake.cakequakeback.schedule.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
public class ShopScheduleRequestDTO {
    private Long shopId;
    private LocalDateTime scheduleDateTime;

    public ShopScheduleRequestDTO(Long shopId, LocalDateTime scheduleDateTime) {
        this.shopId = shopId;
        this.scheduleDateTime = scheduleDateTime;
    }
}
