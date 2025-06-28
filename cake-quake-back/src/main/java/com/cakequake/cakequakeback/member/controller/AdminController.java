package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerPageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerRequestListDTO;
import com.cakequake.cakequakeback.member.service.admin.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // 대기 중인 판매자 목록 조회
    @GetMapping("/sellers/pending")
    public ResponseEntity<InfiniteScrollResponseDTO<PendingSellerRequestListDTO>> pendingSellerList(PendingSellerPageRequestDTO requestDTO) {
        log.debug("---AdminController---pendingSellerList---");

        InfiniteScrollResponseDTO<PendingSellerRequestListDTO> list = adminService.pendingSellerRequestList(requestDTO);
        return ResponseEntity.ok(list);
    }

    // 판매자 승인
    @PostMapping("/sellers/{tempSellerId}/approve")
    public ResponseEntity<ApiResponseDTO> approvePendingSeller(@PathVariable Long tempSellerId) {
        log.debug("---AdminController---approvePendingSeller--- tempSellerId: {}", tempSellerId);

        ApiResponseDTO response = adminService.approvePendingSeller(tempSellerId);
        return ResponseEntity.ok(response);
    }

    // 판매자 보류
    @PatchMapping("/sellers/{tempSellerId}/hold")
    public ResponseEntity<ApiResponseDTO> holdPendingSeller(@PathVariable Long tempSellerId) {
        log.debug("---AdminController---holdPendingSeller--- tempSellerId: {}", tempSellerId);

        ApiResponseDTO response = adminService.holdPendingSellerStatus(tempSellerId);
        return ResponseEntity.ok(response);
    }

    // 판매자 거절
    @PatchMapping("/sellers/{tempSellerId}/reject")
    public ResponseEntity<ApiResponseDTO> rejectPendingSeller(@PathVariable Long tempSellerId) {
        log.debug("---AdminController---rejectPendingSeller--- tempSellerId: {}", tempSellerId);

        ApiResponseDTO response = adminService.rejectPendingSellerStatus(tempSellerId);
        return ResponseEntity.ok(response);
    }

}
