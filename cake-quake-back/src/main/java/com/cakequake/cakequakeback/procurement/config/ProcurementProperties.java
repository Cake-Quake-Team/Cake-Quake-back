// src/main/java/com/cakequake/cakequakeback/procurement/config/ProcurementProperties.java
package com.cakequake.cakequakeback.procurement.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "procurement")
@Data
public class ProcurementProperties {

     //컷오프 시각 (0~23)
    private int cutoffHour;


     // 배송 소요 일수 (연속일)
    private int transitDays;
}
