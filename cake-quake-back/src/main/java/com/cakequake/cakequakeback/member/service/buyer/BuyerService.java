package com.cakequake.cakequakeback.member.service.buyer;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;

public interface BuyerService {

    ApiResponseDTO getBuyerProfile(Long uid);
}
