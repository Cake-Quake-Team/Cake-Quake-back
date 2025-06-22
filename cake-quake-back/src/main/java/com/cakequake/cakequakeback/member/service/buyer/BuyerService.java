package com.cakequake.cakequakeback.member.service.buyer;

import com.cakequake.cakequakeback.member.dto.AlarmSettingsDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerModifyDTO;

public interface BuyerService {

    ApiResponseDTO getBuyerProfile(Long uid);

    ApiResponseDTO modifyBuyerProfile(Long uid, BuyerModifyDTO dto);

    ApiResponseDTO modifyBuyerAlarm(Long uid, AlarmSettingsDTO alarmSettingsDTO);

    ApiResponseDTO withdrawBuyer(); // 탈퇴
}
