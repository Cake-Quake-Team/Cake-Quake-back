package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.service.seller.SellerService;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/sellers")
@Slf4j
public class SellerController {

    private final SellerService sellerService;
    private final AuthenticatedUserService authenticatedUserService;

    public SellerController(SellerService sellerService, AuthenticatedUserService authenticatedUserService) {
        this.sellerService = sellerService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDTO> getSellerProfile() {

        Long uid = authenticatedUserService.getCurrentMemberId(); // 로그인 한 유저의 uid
        log.debug("---SellerController---getSellerProfile---uid: {}", uid);

        ApiResponseDTO response = sellerService.getSellerProfile(uid);
        return ResponseEntity.ok(response);
    }
}
