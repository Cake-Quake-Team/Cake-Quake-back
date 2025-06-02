package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.payment.dto.KakaoPayApproveResponse;
import com.cakequake.cakequakeback.payment.dto.KakaoPayCancelResponseDTO;
import com.cakequake.cakequakeback.payment.dto.KakaoPayReadyResponseDTO;

import java.math.BigDecimal;

public interface KakaoPayService {
    //결제 준비 요청
    KakaoPayReadyResponseDTO ready(Long shopId, Long orderId, BigDecimal amount);

    //결체 취소(환불) 요청
    KakaoPayCancelResponseDTO cancel(Long shopId, String tid, BigDecimal amount);

    KakaoPayApproveResponse approvePayment(String tid, String patnerOrderId, String partnerUserId, String pgToken);
}
