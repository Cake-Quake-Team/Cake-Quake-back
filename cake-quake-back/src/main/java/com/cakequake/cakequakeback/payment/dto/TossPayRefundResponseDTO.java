package com.cakequake.cakequakeback.payment.dto;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TossPayRefundResponseDTO {
    /** 토스페이가 발급한 결제 키(paymentKey) */
    private String paymentKey;

    /** 가맹점 주문번호 (merchantOrderId) */
    private String orderId;

    /** 주문명 */
    private String orderName;

    /** 실제 결제 금액(원 단위) */
    private Integer requestedAmount;

    /** 요청된 환불 금액(원 단위) */
    private Integer refundedAmount;

    /** 환불 처리 상태 ("DONE", "FAILED" 등) */
    private String status;

    /** 환불 요청 시각 (ISO 8601) */
    private String requestedAt;

    /** 환불 완료 시각 (ISO 8601, 완료된 경우) */
    private String approvedAt;

    /** 환불 사유 (refund reason) */
    private String reason;

    // (필요하다면 토스페이가 반환하는 추가 필드를 여기에 선언)
}
