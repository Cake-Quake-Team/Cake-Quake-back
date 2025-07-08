package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerModifyDTO;
import com.cakequake.cakequakeback.member.service.seller.SellerService;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sellers")
@Slf4j
@PreAuthorize("hasRole('SELLER')")
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

        ApiResponseDTO response = sellerService.getSellerProfile(uid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile/{uid}")
    public ResponseEntity<ApiResponseDTO> modifySellerProfile(@PathVariable Long uid, @RequestBody SellerModifyDTO dto) {

        ApiResponseDTO response = sellerService.modifySellerProfile(uid, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/withdraw")
    public ResponseEntity<ApiResponseDTO> withdrawSeller() {
        log.debug("---SellerController---withdrawSeller---");

        ApiResponseDTO response = sellerService.withdrawSeller();
        return ResponseEntity.ok(response);
    }

}
