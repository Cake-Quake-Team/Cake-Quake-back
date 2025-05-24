package com.cakequake.cakequakeback.schedule.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;


@Data
@NoArgsConstructor
@Builder

public class ShopScheduleDTO {

    private LocalDate availableDate;
    private LocalTime startTime;
    private String description;

    public ShopScheduleDTO(LocalDate availableDate, LocalTime startTime, String description) {
        this.availableDate = availableDate;
        this.startTime = startTime;
        this.description = description;
    }
}
