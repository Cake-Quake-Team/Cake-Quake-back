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
    private NextUrl nextUrl;      // 중첩 객체로 선언

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    public static class NextUrl {
        private String web;
        private String mobile;
    }
}
