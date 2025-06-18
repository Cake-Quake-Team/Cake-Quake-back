package com.cakequake.cakequakeback.temperature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class TemperatureDetailResponseDTO {
    private double currentTemperature;
    private List<TemperatureHistoryResponseDTO> history;
}
