package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.service.MemberService;
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
@RequestMapping("/api/auth")
@Slf4j
public class MemberAuthController {

    private final MemberService memberService;

    @PostMapping("/signup/buyers")
    public ResponseEntity<ApiResponseDTO> signupBuyer(@RequestBody @Valid BuyerSignupRequestDTO dto) {

        // service에서 joinType에 따라 분기 처리. basic/kakao/google
        ApiResponseDTO response = memberService.signup(dto);

        return ResponseEntity.ok(response);
    }
}
