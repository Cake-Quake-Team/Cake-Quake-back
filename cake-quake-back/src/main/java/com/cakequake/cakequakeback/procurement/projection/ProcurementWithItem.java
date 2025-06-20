package com.cakequake.cakequakeback.procurement.projection;

import com.cakequake.cakequakeback.procurement.entities.ProcurementStatus;

import java.time.LocalDateTime;

public interface ProcurementWithItem {
    Long getProcurementId();

    Long getShopId();

    ProcurementStatus getStatus();

    String getNote();

    LocalDateTime getScheduledDate();

    LocalDateTime getRegDate();

    Long getProcurementItemId();

    Long getIngredientId();

    Integer getQuantity();
}