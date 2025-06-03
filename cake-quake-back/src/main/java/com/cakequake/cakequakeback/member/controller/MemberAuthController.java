package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep1RequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep2RequestDTO;
import com.cakequake.cakequakeback.member.service.MemberService;
import com.cakequake.cakequakeback.member.service.seller.SellerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class MemberAuthController {

    private final MemberService memberService;
    private final SellerService sellerService;

    public MemberAuthController(MemberService memberService, SellerService sellerService) {
        this.memberService = memberService;
        this.sellerService = sellerService;
    }

    @PostMapping("/signup/buyers")
    public ResponseEntity<ApiResponseDTO> signupBuyer(@RequestBody @Valid BuyerSignupRequestDTO dto) {
        // service에서 joinType에 따라 분기 처리. basic/kakao/google
        ApiResponseDTO response = memberService.signup(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/sellers/step1")
    public ResponseEntity<ApiResponseDTO> signupSellerStep1(@ModelAttribute SellerSignupStep1RequestDTO dto) {
        log.info("---MemberAuthController---SellerSignupStep1RequestDTO: {}", dto.toString());
        ApiResponseDTO response = sellerService.registerStepOne(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/sellers/step2")
    public ResponseEntity<ApiResponseDTO> signupSellerStep2(@ModelAttribute SellerSignupStep2RequestDTO dto) {
        log.info("---MemberAuthController---SellerSignupStep2RequestDTO: {}", dto.toString());
        ApiResponseDTO response = sellerService.registerStepTwo(dto);
        return ResponseEntity.ok(response);
    }


}
