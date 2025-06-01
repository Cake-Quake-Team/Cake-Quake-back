package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.payment.dto.TossPayApproveResponseDTO;
import com.cakequake.cakequakeback.payment.dto.TossPayCancelRequestDTO;
import com.cakequake.cakequakeback.payment.dto.TossPayCancelResponseDTO;
import com.cakequake.cakequakeback.payment.dto.TossPayReadyResponseDTO;

public interface TossPayService {
    TossPayReadyResponseDTO requestPayment(Long shopId, Long orderId, String customerKey, Long amount);

    TossPayApproveResponseDTO approve(String paymentKey);

    TossPayCancelResponseDTO cancel(Long shopId, TossPayCancelRequestDTO cancelRequest);
}
