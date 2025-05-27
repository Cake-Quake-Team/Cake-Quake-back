package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationCheckDTO;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationRequestDTO;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationResponseDTO;
import com.cakequake.cakequakeback.member.service.PhoneVerificationService;
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
@RequestMapping("/api/auth/otp")
public class PhoneVerificationController {

    private final PhoneVerificationService service;

    // 인증 코드 발송
    @PostMapping("/send")
    public ResponseEntity<PhoneVerificationResponseDTO> sendVerificationCode(@RequestBody PhoneVerificationRequestDTO requestDTO) {
        service.sendVerificationCode(requestDTO);

        return ResponseEntity.ok(PhoneVerificationResponseDTO.builder()
                .success(true)
                .message("인증번호 발송 성공")
                .build());
    }

    @PostMapping("/verify")
    public ResponseEntity<PhoneVerificationResponseDTO> verifyCode(@RequestBody PhoneVerificationCheckDTO checkDTO) {
        service.verifyCode(checkDTO);

        return ResponseEntity.ok(PhoneVerificationResponseDTO.builder()
                .success(true)
                .message("인증번호 검증 성공")
                .build());
    }
}
