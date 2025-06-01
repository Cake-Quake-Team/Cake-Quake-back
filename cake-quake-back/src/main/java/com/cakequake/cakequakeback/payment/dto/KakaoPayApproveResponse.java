package com.cakequake.cakequakeback.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KakaoPayApproveResponse {
    private String aid;
    private String tid;
    private String cid;
    private String partner_order_id;
    private String partner_user_id;

    @JsonProperty("payment_method_type")
    private String paymentMethodType;

    // 중략: amount 객체는 필요한 경우 하위 클래스로 매핑 가능
    private Amount amount;

    @JsonProperty("approved_at")
    private String approvedAt;

    // 필요하다면 payload 등 다른 필드도 추가

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Amount {
        private BigDecimal total;
        private BigDecimal tax_free;
        private BigDecimal vat;
        private BigDecimal point;
        private BigDecimal discount;
    }

}

