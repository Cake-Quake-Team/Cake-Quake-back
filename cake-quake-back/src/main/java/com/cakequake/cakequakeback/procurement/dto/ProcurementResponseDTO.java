package com.cakequake.cakequakeback.procurement.dto;

import com.cakequake.cakequakeback.procurement.entities.ProcurementStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementResponseDTO{
    //매장 및 관리자가 요청 현황을 조죄할 때 반환되는 응답

    private Long procurementId;
    private Long shopId;
    private ProcurementStatus status;
    private String note;
    private LocalDateTime scheduleDate;
    private LocalDateTime regDate;
    private List<ProcurementItemResponseDTO> items;
}
