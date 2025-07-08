package com.cakequake.cakequakeback.procurement.controller;


import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.CancelProcurementDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementResponseDTO;
import com.cakequake.cakequakeback.procurement.entities.ProcurementStatus;
import com.cakequake.cakequakeback.procurement.service.ProcurementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ProcurementController {
    private final ProcurementService procurementService;

    //매장별 요청 내역 조회
    // /api/shops/{shopId}/procurements
    @GetMapping("/shops/{shopId}/procurements")
    public ResponseEntity<InfiniteScrollResponseDTO<ProcurementResponseDTO>> getProcurementList(
            @PathVariable Long shopId,
            @ModelAttribute@Valid PageRequestDTO pageRequestDTO) {
        return ResponseEntity.ok(procurementService.getStoreRequests(pageRequestDTO,shopId));
    }

    //상태별 요청 내역 조회
    // /api/procurements/status/{status}
    @GetMapping("/procurements/status/{status}")
    public ResponseEntity<InfiniteScrollResponseDTO<ProcurementResponseDTO>> getProcurementStatusList(
            @PathVariable ProcurementStatus status,
            @ModelAttribute@Valid PageRequestDTO pageRequestDTO
    ){
        return ResponseEntity.ok(procurementService.getRequestsByStatus(pageRequestDTO,status));
    }

    //매장 + 상태 복합 조건 조회
    // /api/shops/{shopId}/procurements/status/{status}
    @GetMapping("/shops/{shopId}/procurements/status/{status}")
    public ResponseEntity<InfiniteScrollResponseDTO<ProcurementResponseDTO>> getByShopAndStatusList(
            @PathVariable Long shopId,
            @PathVariable ProcurementStatus status,
            @ModelAttribute@Valid PageRequestDTO pageRequestDTO
    ){
        return ResponseEntity.ok(procurementService.getStoreRequestsByStatus(pageRequestDTO,shopId,status));
    }

    //단건 조회
    // /api/shops/{shopId}/procurements/{procurementId}
    @GetMapping("/shops/{shopId}/procurements/{procurementId}")
    public ResponseEntity<ProcurementResponseDTO> getProcurement(
            @PathVariable Long shopId,
            @PathVariable ("procurementId") Long procurementId
    ){
        return ResponseEntity.ok(procurementService.getRequest(shopId,procurementId));
    }


    //신규 요청 생성
    // /api/shops/{shopId}/procurements
    @PostMapping("/shops/{shopId}/procurements")
    public ResponseEntity<ProcurementResponseDTO> create(
            @PathVariable Long shopId,
            @RequestBody @Valid ProcurementRequestDTO procurementRequestDTO
    ){
        procurementRequestDTO.setShopId(shopId);
        return ResponseEntity.ok(procurementService.createProcurement(procurementRequestDTO));
    }
    @PostMapping("/shops/{shopId}/procurements/{procurementId}/cancel")
    public ResponseEntity<ProcurementResponseDTO> cancelBySeller(
            @PathVariable Long shopId,
            @PathVariable Long procurementId,
            @RequestBody @Valid CancelProcurementDTO cancelDto
    ) {
        ProcurementResponseDTO response = procurementService.cancelBySeller(shopId, procurementId, cancelDto);
        return ResponseEntity.ok(response);
    }


    //-------------------관리자-----------------
    //관리자 확정(일정 지정)
    // /api/procurements/{procurementId}/confirm
//    @PostMapping("/procurements/{procurementId}/confirm")
//    public ResponseEntity<ProcurementResponseDTO> confirm(
//            @PathVariable("procurementId") Long procurementId,
//            @RequestBody@Valid ConfirmProcurementDTO confirmDTO
//    ){
//        return ResponseEntity.ok(procurementService.confirmProcurement(procurementId,confirmDTO));
//    }

    //관리자용 발주 전체 조회
    @GetMapping("/procurements")
    public ResponseEntity<InfiniteScrollResponseDTO<ProcurementResponseDTO>>getAllProcurements(
            @ModelAttribute@Valid PageRequestDTO pageRequestDTO
    ){
        return ResponseEntity.ok(procurementService.getAllRequests(pageRequestDTO));
    }

    //관리자용 발주 단건 조회
    @GetMapping("/procurements/{procurementId}")
    public ResponseEntity<ProcurementResponseDTO>getProcurementById(
            @PathVariable Long procurementId
    ){
        ProcurementResponseDTO dto = procurementService.getRequestById(procurementId);
        return ResponseEntity.ok(dto);
    }

    // 2) 관리자: 전체 발주를 (권한만료전까지) 취소
    //    POST /api/procurements/{procurementId}/cancel
    @PostMapping("/procurements/{procurementId}/cancel")
    public ResponseEntity<ProcurementResponseDTO> cancelByAdmin(
            @PathVariable Long procurementId,
            @RequestBody @Valid CancelProcurementDTO cancelDto
    ) {
        ProcurementResponseDTO response = procurementService.cancelByAdmin(procurementId, cancelDto);
        return ResponseEntity.ok(response);
    }

}
