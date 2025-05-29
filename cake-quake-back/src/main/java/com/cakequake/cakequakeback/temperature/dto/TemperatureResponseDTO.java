package com.cakequake.cakequakeback.temperature.dto;

import com.cakequake.cakequakeback.temperature.entities.Grade;
import lombok.*;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor

public class TemperatureResponseDTO {
    private Long memberId;
    private double temperature;
    private Grade grade;

}
