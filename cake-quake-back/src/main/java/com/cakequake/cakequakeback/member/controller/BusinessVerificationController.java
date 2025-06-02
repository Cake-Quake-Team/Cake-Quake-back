package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationRequestDTO;
import com.cakequake.cakequakeback.member.service.seller.BusinessVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth/business")
public class BusinessVerificationController {

    private final BusinessVerificationService service;

    @PostMapping("/verify")
    public ResponseEntity<ApiResponseDTO> verifyBusiness(@RequestBody @Valid BusinessVerificationRequestDTO requestDTO) {
        log.debug("---controller---사업자 등록 정보 진위확인 요청: {}", requestDTO);

        ApiResponseDTO response = service.verify(requestDTO);

        return ResponseEntity.ok(response);
    }

}
