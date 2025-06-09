package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationCheckDTO;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationRequestDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.service.PhoneVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/otp")
public class PhoneVerificationController {

    private final PhoneVerificationService service;

    // 인증 코드 발송
    @PostMapping("/send")
    public ResponseEntity<ApiResponseDTO> sendVerificationCode(@RequestBody @Valid PhoneVerificationRequestDTO requestDTO) {

        ApiResponseDTO response = service.sendVerificationCode(requestDTO);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiResponseDTO> verifyCode(@RequestBody @Valid PhoneVerificationCheckDTO checkDTO) {

        ApiResponseDTO response = service.verifyCode(checkDTO);

        return ResponseEntity.ok(response);
    }
}
