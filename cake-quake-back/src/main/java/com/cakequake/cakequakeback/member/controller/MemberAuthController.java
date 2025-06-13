package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.*;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep1RequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep2RequestDTO;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.service.MemberService;
import com.cakequake.cakequakeback.member.service.seller.SellerService;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@Slf4j
public class MemberAuthController {

    private final MemberService memberService;
    private final SellerService sellerService;
    private final AuthenticatedUserService authenticatedUserService;

    public MemberAuthController(MemberService memberService, SellerService sellerService, AuthenticatedUserService authenticatedUserService) {
        this.memberService = memberService;
        this.sellerService = sellerService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @PostMapping("/signup/buyers")
    public ResponseEntity<ApiResponseDTO> signupBuyer(@RequestBody @Valid BuyerSignupRequestDTO dto) {
        log.debug(dto.toString());
        // service에서 joinType에 따라 분기 처리. basic/kakao/google
        ApiResponseDTO response = memberService.signup(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/sellers/step1")
    public ResponseEntity<ApiResponseDTO> signupSellerStep1(@ModelAttribute SellerSignupStep1RequestDTO dto) {
        log.debug("---MemberAuthController---SellerSignupStep1RequestDTO: {}", dto.toString());
        ApiResponseDTO response = sellerService.registerStepOne(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signup/sellers/step2")
    public ResponseEntity<ApiResponseDTO> signupSellerStep2(@ModelAttribute SellerSignupStep2RequestDTO dto) {
        log.debug("---MemberAuthController---SellerSignupStep2RequestDTO: {}", dto.toString());
        ApiResponseDTO response = sellerService.registerStepTwo(dto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/signin")
    public ResponseEntity<SigninResponseDTO> signin(@RequestBody @Valid SigninRequestDTO request) {

        return ResponseEntity.ok(memberService.signin(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<RefreshTokenResponseDTO> refresh(@RequestHeader("Authorization") String accessTokenStr,
                                                           @RequestBody RefreshTokenRequestDTO requestDTO) {
        log.debug("---MemberAuthController---refresh()");

        String accessToken = accessTokenStr.substring(7);

        return ResponseEntity.ok(memberService.refreshTokens(accessToken, requestDTO));
    }

    @PostMapping("/signout")
    public ResponseEntity<Void> signout(HttpServletResponse response) {
        log.debug("---MemberAuthController---signout()");

        return ResponseEntity.ok().build();
    }

    /*
        테스트 용
     */
//    @PreAuthorize("hasRole('BUYER')")
    @GetMapping("/token-test")
    public ResponseEntity<ApiResponseDTO> tokenTest(){

        Member member = authenticatedUserService.getCurrentMember();
        log.debug("UserId: {}, Uid: {}", member.getUserId(), member.getUid());

        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(true)
                .message("로그인 후 토큰으로 테스트 접근 성공")
//                        .data(member)
                .build());
    }

    @PreAuthorize("hasRole('SELLER')")
    @GetMapping("/seller-only")
    public ResponseEntity<ApiResponseDTO> sellerOnly(@AuthenticationPrincipal CustomUserDetails userDetails){

        log.debug("--------sellerOnly()-----------");
        String role = userDetails.getAuthorities().toString();

        log.debug("Role: {}", role);

        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(true)
                .message("판매자만 접근 성공")
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin-only")
    public ResponseEntity<ApiResponseDTO> adminOnly(){
        return ResponseEntity.ok(ApiResponseDTO.builder()
                .success(true)
                .message("관리자만 접근 성공")
                .build());
    }



}
