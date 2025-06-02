package com.cakequake.cakequakeback.payment.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoPayCancelResponseDTO {
    private String aid;    //요청 고유 번호
    private String tid;    //결제 고유 번호
    private String cid;   //가맹점 코드
    @JsonProperty("status")
    private String status;                      // 요청 상태 (e.g. "CANCELLED")
    @JsonProperty("cancel_amount")
    private int cancelAmount;                   // 취소 금액
    @JsonProperty("cancel_tax_free_amount")
    private int cancelTaxFreeAmount;            // 비과세 취소 금액
    @JsonProperty("created_at")
    private String createdAt;                   // 취소 완료 시각 ISO8601
}
