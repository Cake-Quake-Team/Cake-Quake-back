package com.cakequake.cakequakeback.procurement.dto.procurement;

import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CancelProcurementDTO {
    @NotNull(message = "취소 사유를 입력해주세요")
    private String reason;
}
