package com.cakequake.cakequakeback.procurement.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.CancelProcurementDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementResponseDTO;
import com.cakequake.cakequakeback.procurement.entities.Procurement;
import com.cakequake.cakequakeback.procurement.entities.ProcurementStatus;

public interface ProcurementService {
    //매장별 무한 스크롤 조회
    InfiniteScrollResponseDTO<ProcurementResponseDTO> getStoreRequests(PageRequestDTO pageRequestDTO, Long shopId);

    /**
     * 상태별 무한 스크롤 조회
     */
    InfiniteScrollResponseDTO<ProcurementResponseDTO> getRequestsByStatus(PageRequestDTO pageRequestDTO, ProcurementStatus status);

    /**
     * 매장+상태 복합 무한 스크롤 조회
     */
    InfiniteScrollResponseDTO<ProcurementResponseDTO> getStoreRequestsByStatus(
            PageRequestDTO pageRequestDTO, Long shopId, ProcurementStatus status);


    //매장 단건 조회
    ProcurementResponseDTO getRequest(
            Long shopId,
            Long procurementId
    );

    /**
     * 새로운 요청 생성
     */
    ProcurementResponseDTO createProcurement(ProcurementRequestDTO request);



    //관리자 발주 전체 조회
    InfiniteScrollResponseDTO<ProcurementResponseDTO> getAllRequests(PageRequestDTO pageRequestDTO);

    //관리자 발주 단건 조회
    ProcurementResponseDTO getRequestById(Long procurementId);

    //발주 취소 공통 로직
    ProcurementResponseDTO cancelProcurement(Procurement procurement, CancelProcurementDTO cancelDTO);

    //판매자 발주 취소 검증
    ProcurementResponseDTO cancelBySeller(Long shopId, Long procurementId, CancelProcurementDTO cancelDTO);

    //관리자 발주 취소 검증
    ProcurementResponseDTO cancelByAdmin(Long procurementId, CancelProcurementDTO cancelDTO);







}
