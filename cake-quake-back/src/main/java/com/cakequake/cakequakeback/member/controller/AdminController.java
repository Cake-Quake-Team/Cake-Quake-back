package com.cakequake.cakequakeback.member.controller;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.member.dto.admin.PendingSellerRequestListDTO;
import com.cakequake.cakequakeback.member.service.admin.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin")
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/sellers/pending")
    public ResponseEntity<InfiniteScrollResponseDTO<PendingSellerRequestListDTO>> pendingSellerList(PageRequestDTO requestDTO) {
        log.debug("---AdminController---pendingSellerList---");

        InfiniteScrollResponseDTO<PendingSellerRequestListDTO> list = adminService.pendingSellerRequestList(requestDTO);

        return ResponseEntity.ok(list);
    }
}
