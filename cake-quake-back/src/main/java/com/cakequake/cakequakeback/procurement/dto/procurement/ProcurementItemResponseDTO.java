package com.cakequake.cakequakeback.procurement.dto.procurement;

import lombok.*;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementItemResponseDTO {
    //매장 요청 및 관ㄹ지ㅏ가 조회할 때 각 재료 항목별 응답정보

    private Long itemId;
    private Long ingredientId;
    private String ingredientName;
    private String unit;
    private Integer unitPrice;
    private Integer quantity;

}
