package com.cakequake.cakequakeback.member.service.auth;

import com.cakequake.cakequakeback.member.dto.*;
import com.cakequake.cakequakeback.member.dto.auth.*;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;

public interface MemberService {

    ApiResponseDTO signup(BuyerSignupRequestDTO requestDTO);

    SigninResponseDTO signin(SigninRequestDTO requestDTO);

    RefreshTokenResponseDTO refreshTokens(String accessToken, RefreshTokenRequestDTO requestDTO);

    ApiResponseDTO changePassword(PasswordChangeDTO dto);

}
