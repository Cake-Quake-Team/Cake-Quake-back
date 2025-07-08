package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.member.dto.AlarmSettingsDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerModifyDTO;
import com.cakequake.cakequakeback.member.service.buyer.BuyerService;
import com.cakequake.cakequakeback.security.service.AuthenticatedUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyers")
@Slf4j
@PreAuthorize("hasRole('BUYER')")
public class BuyerController {

    private final BuyerService buyerService;
    private final AuthenticatedUserService authenticatedUserService;

    public BuyerController(BuyerService buyerService, AuthenticatedUserService authenticatedUserService) {
        this.buyerService = buyerService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping("/profile")
    public ResponseEntity<ApiResponseDTO> getBuyerProfile() {

        Long uid = authenticatedUserService.getCurrentMemberId(); // 로그인 한 유저의 uid
//        log.debug("---BuyerController---getSellerProfile---uid: {}", uid);

        ApiResponseDTO response = buyerService.getBuyerProfile(uid);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile/{uid}")
    public ResponseEntity<ApiResponseDTO> modifyBuyerProfile(@PathVariable Long uid, @RequestBody BuyerModifyDTO dto) {
//        log.debug("---BuyerController---modifySellerProfile---uid: {}", uid);

        ApiResponseDTO response = buyerService.modifyBuyerProfile(uid, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/profile/alarm/{uid}")
    public ResponseEntity<ApiResponseDTO> modifyBuyerAlarm(@PathVariable Long uid, @RequestBody AlarmSettingsDTO dto) {
//        log.debug("---BuyerController---modifyBuyerAlarm---uid: {}", uid);

        ApiResponseDTO response = buyerService.modifyBuyerAlarm(uid, dto);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/withdraw")
    public ResponseEntity<ApiResponseDTO> withdrawBuyer() {
        log.debug("---BuyerController---withdrawBuyer---");

        ApiResponseDTO response = buyerService.withdrawBuyer();
        return ResponseEntity.ok(response);
    }

}
