package com.cakequake.cakequakeback.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TossPayCancelResponseDTO {

    // 토스페이가 발급한 고유 결제 키
    private String paymentKey;


    //가맹점 주문번호
    private String orderId;


    //원래 결제할 금액
    private int amount;


    // 환불된 금액
    private int refundedAmount;

    // 환불 처리 상태 ("CANCELLED" 등)
    private String status;


    // 환불 처리 시각 (ISO 8601)
    @JsonProperty("cancelledAt")
    private String cancelledAt;
}
// (필요 시 추가 필드 선언)
