package com.cakequake.cakequakeback.payment.dto;

import com.cakequake.cakequakeback.payment.entities.Payment;
import com.cakequake.cakequakeback.payment.entities.PaymentProvider;
import com.cakequake.cakequakeback.payment.entities.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PaymentResponseDTO {
    // 결제 아이디
    private long paymentId;


    private PaymentProvider provider;

    //결제 수단
    private PaymentStatus status;

    //결제 금액
    private BigDecimal amount;

    //PG사 거래 식별자 (tid or paymentKet)
    private String transactionId;

    //결제 요청 시각
    private LocalDateTime regDate;

    //결제 완료 시각
    private LocalDateTime completedAt;

    //결제 취소 사유
    private String cancelReason;

    //환불 시각
    private LocalDateTime refundAt;

    //환불 사유
    private String refundReason;

    //카카오 페이 결제 키 (provider == Toss 일 때)
    private String redirectUrl;

    //토스페이 결제 키 (provider == Toss 일 때)
    private String paymentUrl;


    public static PaymentResponseDTO fromEntity(Payment payment) {
        return PaymentResponseDTO.builder()
                .paymentId(payment.getPaymentId())
                .provider(payment.getProvider())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .regDate(payment.getRegDate())
                .completedAt(payment.getCompletedAt())
                .cancelReason(payment.getCancelReason())
                .refundAt(payment.getRefundAt())
                .refundReason(payment.getRefundReason())
                .redirectUrl(payment.getRedirectUrl())
                .paymentUrl(payment.getPaymentUrl())
                .build();
    }
}
