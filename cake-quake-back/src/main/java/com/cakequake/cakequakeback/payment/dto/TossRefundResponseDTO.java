package com.cakequake.cakequakeback.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TossRefundResponseDTO {
    private String refundKey;                    // 환불 키
    private String paymentKey;                   // 원결제 키
    private int canceledAmount;                  // 환불 금액
    private String canceledAt;                   // 환불 완료 시각
    private String status;                       // 환불 상태 ("CANCELED")
    @JsonProperty("requestedAt")
    private String requestedAt;                  // 환불 요청 시각
}
