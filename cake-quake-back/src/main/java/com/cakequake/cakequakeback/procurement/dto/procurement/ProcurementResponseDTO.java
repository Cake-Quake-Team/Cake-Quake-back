package com.cakequake.cakequakeback.procurement.dto.procurement;

import com.cakequake.cakequakeback.procurement.entities.ProcurementStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private String shopName;
    private ProcurementStatus status;
    private String note;
    private LocalDate estimatedArrivalDate;
    private LocalDateTime regDate;
    private String cancelReason;
    private List<ProcurementItemResponseDTO> items;
    private BigDecimal totalPrice;


    public ProcurementResponseDTO(
            Long procurementId,
            Long shopId,
            String shopName,
            ProcurementStatus status,
            String note,
            LocalDate estimatedArrivalDate,
            LocalDateTime regDate,
            String cancelReason,
            List<ProcurementItemResponseDTO> items
    ) {
        this.procurementId = procurementId;
        this.shopId        = shopId;
        this.shopName      = shopName;
        this.status        = status;
        this.note          = note;
        this.estimatedArrivalDate  = estimatedArrivalDate;
        this.regDate       = regDate;
        this.cancelReason  = cancelReason;
        this.items         = items;
        this.totalPrice    = BigDecimal.ZERO;
    }
}
