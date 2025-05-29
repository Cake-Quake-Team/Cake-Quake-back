package com.cakequake.cakequakeback.temperature.service;

import com.cakequake.cakequakeback.temperature.dto.TemperatureHistoryResponseDTO;
import com.cakequake.cakequakeback.temperature.dto.TemperatureRequestDTO;
import com.cakequake.cakequakeback.temperature.dto.TemperatureResponseDTO;
import com.cakequake.cakequakeback.temperature.entities.Temperature;

import java.util.List;

public interface TemperatureService {
    public void updateTemperature(Long orderId, Long reviewId);
    public List<TemperatureHistoryResponseDTO> findHistory(Long uid);
    public void decreaseCancle(Long orderId);
    public void decreaseNoShow(Long orderId);
    public void increasePickup(Long orderId);
    public void increaseReview(Long orderId);
    public Temperature getTemperatureByUid(Long uid);
    public void updateByuid(TemperatureRequestDTO request);


}
