package com.cakequake.cakequakeback.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPayCancelRequestDTO {
    /** 환불(취소)할 paymentKey */
   // private String paymentKey;

    /** 환불할 금액(원 단위) */
    private Long cancelAmount;

    /** 환불 사유 */
    private String cancelReason;
}
