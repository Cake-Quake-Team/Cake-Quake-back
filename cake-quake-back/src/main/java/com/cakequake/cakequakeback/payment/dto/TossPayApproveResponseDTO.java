package com.cakequake.cakequakeback.payment.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPayApproveResponseDTO {
    /** 토스페이가 발급한 고유 결제 키 */
    private String paymentKey;

    /** 가맹점 주문번호 */
    private String orderId;

    /** 주문명 */
    private String orderName;

    /** 결제할 금액 */
    private int amount;

    /** 결제 상태 ("DONE" 등) */
    private String status;

    /** 결제 수단 코드 ("CARD" 등) */
    private String method;

    /** 결제 준비 요청 시각 */
    private String requestedAt;

    /** 결제 승인 시각 (ISO 8601) */
    private String approvedAt;

    /** 결제 취소 시각 (승인 이후 환불 시) */
    @JsonProperty("cancelledAt")
    private String cancelledAt;

    // (필요하다면 추가 필드를 여기에 선언)
}
