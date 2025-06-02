package com.cakequake.cakequakeback.payment.dto;

import com.cakequake.cakequakeback.payment.entities.PaymentProvider;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@Data
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequestDTO {
    @NotNull
    private Long shopId;

    @NotNull
    private Long orderId;

    @NotNull
    private PaymentProvider provider;

    @Positive
    @NotNull
    private BigDecimal amount;
}
