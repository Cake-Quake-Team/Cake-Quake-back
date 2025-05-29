package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationCheckDTO;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationRequestDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;

public interface PhoneVerificationService {

    ApiResponseDTO sendVerificationCode(PhoneVerificationRequestDTO requestDTO); // 인증번호 전송

    ApiResponseDTO verifyCode(PhoneVerificationCheckDTO checkDTO); // 인증번호 검증
}
