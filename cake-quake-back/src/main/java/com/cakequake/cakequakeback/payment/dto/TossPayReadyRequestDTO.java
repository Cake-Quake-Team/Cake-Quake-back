package com.cakequake.cakequakeback.payment.dto;

import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TossPayReadyRequestDTO {
    private String method;      // ex. "FOREIGN_EASY_PAY" 또는 "CARD"

    // 결제할 금액
    private Long amount;

    private String currency;    // ex. "KRW"

    // 가맹점 주문번호
    private String orderId;

    // 주문명(예: 상품명)
    private String orderName;

    private String flowMode;    // ex. "DEFAULT"

    private String easyPay;     // ex. "TOSSPAY"

    // 고객 Key(가맹점 회원 ID 등)
    //private String customerKey;

    // 결제 성공 후 리다이렉트될 URL
    private String successUrl;

    // 결제 실패 후 리다이렉트될 URL
    private String failUrl;

}
