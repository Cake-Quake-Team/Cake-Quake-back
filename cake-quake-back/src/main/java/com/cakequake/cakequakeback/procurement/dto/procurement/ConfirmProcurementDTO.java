package com.cakequake.cakequakeback.procurement.dto.procurement;


import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;


@Data
@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmProcurementDTO {
    //관리자가 매장 요청을 확정하거나 일정을 지정할 때 사용하는 DTO

    @NotNull(message = "일정일자는 필수입니다.")
    @Future(message = "일정일자는 미래여야 합니다.")
    private LocalDate scheduledDate;

}
