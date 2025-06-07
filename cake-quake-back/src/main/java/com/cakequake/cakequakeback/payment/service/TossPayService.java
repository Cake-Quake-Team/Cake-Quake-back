package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.payment.dto.*;

public interface TossPayService {
    TossPayReadyResponseDTO ready(Long orderId, Long userId, String customerKey, Long amount);

    //TossPayApproveResponseDTO approve(String paymentKey);

    TossPayCancelResponseDTO cancel(Long shopId, String paymentKey,TossPayCancelRequestDTO cancelRequest);

    TossPayRefundResponseDTO refund(Long shopId, TossPayRefundRequestDTO refundRequest);
}
