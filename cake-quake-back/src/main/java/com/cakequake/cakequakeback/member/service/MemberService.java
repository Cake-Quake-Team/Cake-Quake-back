package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.*;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;

public interface MemberService {

    ApiResponseDTO signup(BuyerSignupRequestDTO requestDTO);

    SigninResponseDTO signin(SigninRequestDTO requestDTO);

    RefreshTokenResponseDTO refreshTokens(String accessToken, RefreshTokenRequestDTO requestDTO);
}
