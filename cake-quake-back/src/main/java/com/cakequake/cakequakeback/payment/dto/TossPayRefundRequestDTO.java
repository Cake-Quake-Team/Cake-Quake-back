package com.cakequake.cakequakeback.payment.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class TossPayRefundRequestDTO {
    /** 토스페이가 발급한 paymentKey (결제 건 식별자) */
    private String paymentKey;

    /** 환불할 금액 (원 단위, integer) */
    private Long amount;

    /** 환불 사유 */
    private String reason;

    /** (선택) 가맹점 주문번호. 필요 시 추가할 수 있습니다. */
    // private String orderId;
}
