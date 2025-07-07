package com.cakequake.cakequakeback.payment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TossPayReadyResponseDTO {
    private String paymentKey;
    private String orderId;
    private String orderName;
    private int    amount;
    private String status;
    private String method;
    private String requestedAt;
    private String approvedAt;
    // ↓ 실제 JSON 필드명 “checkout” 으로 매핑
    @JsonProperty("checkout")
    private Checkout checkout;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class Checkout {
        // JSON 의 “url” → web, “mobileUrl” → mobile 로 매핑
        @JsonProperty("url")
        private String web;
        @JsonProperty("mobileUrl")
        private String mobile;
    }
}
