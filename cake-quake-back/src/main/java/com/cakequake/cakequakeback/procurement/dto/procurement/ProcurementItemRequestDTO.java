package com.cakequake.cakequakeback.procurement.dto.procurement;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProcurementItemRequestDTO {
    //매장에서 요청할 각 재료의 항목 정보
    @NotNull(message = "재료 ID는 필수입니다.")
    private Long ingredientId;

    @NotNull(message = "수량은 필수입니다.")
    @Min(value =1 ,message ="수량은 1이상이어야 합니다.")
    private Integer quantity;
}
