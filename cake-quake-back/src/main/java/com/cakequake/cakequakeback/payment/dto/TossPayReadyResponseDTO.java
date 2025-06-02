package com.cakequake.cakequakeback.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TossPayReadyResponseDTO {
    private String paymentKey;                  // 결제 키
    private String orderId;                     // 가맹점 주문번호
    private String orderName;                   // 주문명
    private int amount;                         // 결제 금액
    private String status;                      // 결제 상태 ("READY", "DONE" 등)
    private String method;                      // 결제 수단 ("CARD" 등)
    private String requestedAt;                 // 요청 시각
    private String approvedAt;                  // 승인 시각
    @JsonProperty("cancelledAt")
    //결제 화면으로 이동할 URL —★ 이 필드를 반드시 추가하세요!
    private String paymentUrl;
    private String cancelledAt;                 // 취소 시각 (있을 때)
    private String failUrl;                     // 실패 리다이렉트 URL
    private String successUrl;                  // 성공 리다이렉트 URL
    // 추가로 웹훅에서 받는 데이터가 필요하면 여기에 필드 확장
}
