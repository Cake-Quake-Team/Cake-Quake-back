package com.cakequake.cakequakeback.payment.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRefundRequestDTO {
    @NotBlank
    private String reason;
}
