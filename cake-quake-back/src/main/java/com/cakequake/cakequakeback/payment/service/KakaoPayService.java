package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.payment.dto.KakaoPayApproveResponse;
import com.cakequake.cakequakeback.payment.dto.KakaoPayCancelResponseDTO;
import com.cakequake.cakequakeback.payment.dto.KakaoPayReadyResponseDTO;

import java.math.BigDecimal;

public interface KakaoPayService {
    //결제 준비 요청
    KakaoPayReadyResponseDTO ready( Long userId, Long orderId, BigDecimal amount);

    //결체 취소(환불) 요청
    //KakaoPayCancelResponseDTO cancel(Long shopId, String tid, BigDecimal amount);
    KakaoPayCancelResponseDTO cancel (Long paymnetId);


    KakaoPayApproveResponse approvePayment(String tid, String partnerOrderId, String partnerUserId, String pgToken);


}
