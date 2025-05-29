package com.cakequake.cakequakeback.temperature.dto;

import com.cakequake.cakequakeback.temperature.entities.ChangeReason;
import com.cakequake.cakequakeback.temperature.entities.RelatedObjectType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Builder

public class TemperatureRequestDTO {
    private Long uid;
    private double temperature;
    private float change; //변화량
    private ChangeReason reason;
    private RelatedObjectType type;
    private String relatedObjectId;
    LocalDateTime modDate;

    public TemperatureRequestDTO(Long uid, double temperature, float change, ChangeReason reason, RelatedObjectType type, String relatedObjectId, LocalDateTime modDate) {
        this.uid = uid;
        this.temperature = temperature;
        this.change = change;
        this.reason = reason;
        this.type = type;
        this.relatedObjectId = relatedObjectId;
        this.modDate = modDate;
    }
}
