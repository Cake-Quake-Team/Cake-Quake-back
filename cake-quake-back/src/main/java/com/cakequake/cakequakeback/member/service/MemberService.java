package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;

public interface MemberService {

    ApiResponseDTO signup(BuyerSignupRequestDTO requestDTO);
}
