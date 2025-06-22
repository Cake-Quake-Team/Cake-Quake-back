package com.cakequake.cakequakeback.procurement.dto.procurement;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcurementRequestDTO {
    //매장에서 새로운 공동구매 요청을 생성할 때 사용하는 DTO

    @NotNull(message = "매장 ID는 필수입니다.")
    private Long shopId;
    //요청 메모
    private String note;

    @NotEmpty(message = "요청 항목을 최소 하나 이상 선택해야 합니다")
    private List<ProcurementItemRequestDTO> items;
}
