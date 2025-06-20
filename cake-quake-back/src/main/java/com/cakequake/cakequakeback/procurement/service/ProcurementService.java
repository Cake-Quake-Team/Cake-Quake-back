package com.cakequake.cakequakeback.procurement.service;

import com.cakequake.cakequakeback.common.dto.InfiniteScrollResponseDTO;
import com.cakequake.cakequakeback.common.dto.PageRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ConfirmProcurementDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementRequestDTO;
import com.cakequake.cakequakeback.procurement.dto.procurement.ProcurementResponseDTO;
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


    //단건 조회
    ProcurementResponseDTO getRequest(
            Long shopId,
            Long procurementId
    );

    /**
     * 새로운 요청 생성
     */
    ProcurementResponseDTO createProcurement(ProcurementRequestDTO request);

    /**
     * 관리자 확정 (일정 지정)
     */
    ProcurementResponseDTO confirmProcurement(Long procurementId, ConfirmProcurementDTO confirmDto);
}
