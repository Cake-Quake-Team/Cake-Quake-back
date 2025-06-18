package com.cakequake.cakequakeback.temperature.dto;

import com.cakequake.cakequakeback.temperature.entities.ChangeReason;
import com.cakequake.cakequakeback.temperature.entities.RelatedObjectType;
import com.cakequake.cakequakeback.temperature.entities.Temperature;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Getter

public class TemperatureHistoryResponseDTO {

    private Long historyId;
    private Long uid;
    private float changeAmount;
    private double afterTemperature;
    private ChangeReason reason;
    private LocalDateTime regDate;


}
