package com.cakequake.cakequakeback.procurement.dto;

import lombok.*;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementItemResponseDTO {
    //매장 요청 및 관ㄹ지ㅏ가 조회할 때 각 재료 항목별 응답정보

    private Long itemId;
    private Long itemCategoryId;
    private Integer quantity;

}
