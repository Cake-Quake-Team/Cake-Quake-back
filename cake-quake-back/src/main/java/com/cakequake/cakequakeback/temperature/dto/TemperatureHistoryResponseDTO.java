package com.cakequake.cakequakeback.temperature.dto;

import com.cakequake.cakequakeback.temperature.entities.ChangeReason;
import com.cakequake.cakequakeback.temperature.entities.RelatedObjectType;
import com.cakequake.cakequakeback.temperature.entities.Temperature;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder
public class TemperatureHistoryResponseDTO {

    Temperature temperature;
    float changeAmount;
    private double afterTemperature;
    ChangeReason reason;
    RelatedObjectType relatedObjectType;
    String relatedObjectId;
    LocalDateTime modDate;

    public TemperatureHistoryResponseDTO(Temperature temperature, float changeAmount, double afterTemperature, ChangeReason reason, RelatedObjectType relatedObjectType, String relatedObjectId, LocalDateTime modDate) {
        this.temperature = temperature;
        this.changeAmount = changeAmount;
        this.afterTemperature = afterTemperature;
        this.reason = reason;
        this.relatedObjectType = relatedObjectType;
        this.relatedObjectId = relatedObjectId;
        this.modDate = modDate;
    }
}
