package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.payment.dto.*;

public interface TossPayService {
    TossPayReadyResponseDTO requestPayment(Long shopId, Long orderId, String customerKey, Long amount);

    //TossPayApproveResponseDTO approve(String paymentKey);

    TossPayCancelResponseDTO cancel(Long shopId, TossPayCancelRequestDTO cancelRequest);

    TossPayRefundResponseDTO refund(Long shopId, TossPayRefundRequestDTO refundRequest);
}
