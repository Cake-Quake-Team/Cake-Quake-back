package com.cakequake.cakequakeback.member.service.auth;

import com.cakequake.cakequakeback.member.dto.*;
import com.cakequake.cakequakeback.member.dto.auth.*;
import com.cakequake.cakequakeback.member.dto.auth2.SocialSignupRequestDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

public interface MemberService {

    ApiResponseDTO signup(BuyerSignupRequestDTO requestDTO);

    SigninResponseDTO signupSocial(SocialSignupRequestDTO requestDTO);

    SigninResponseDTO signin(SigninRequestDTO requestDTO);

//    RefreshTokenResponseDTO refreshTokens(String accessToken, RefreshTokenRequestDTO requestDTO);
    Map<String, Object> refreshTokens(HttpServletRequest request, HttpServletResponse response);

    ApiResponseDTO changePassword(PasswordChangeDTO dto);

}
