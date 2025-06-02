package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.payment.dto.PaymentCancelRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRefundRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {

    //결제 시작(카카오/토스 구분 -> PG ready 호출 -> DB 저장 -> DTO변환)
    PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequestDTO, Long userId);

    //카카오페이 승인 콜백 처리 (pgToken받고 -> /vi/payment/approve 호출 -> DB 상태 업데이트)
    PaymentResponseDTO approveKakao(String tid, String partnerOrderId, String PartnerUserId, String pgToken);

    // 본인 결제 단건 조회
    PaymentResponseDTO getPayment(Long PaymentID,Long uid);

    //내 결제 내역 전체 조회
    List<PaymentResponseDTO> listPayments(Long uid);

    //구매자 취소
    PaymentResponseDTO cancelPayment(Long paymentId, Long uid, PaymentCancelRequestDTO PaymentCancelRequestDTO);

    //환불 요청
    PaymentResponseDTO refundPayment(Long paymentId, Long uid, PaymentRefundRequestDTO paymentRefundRequestDTO);
}



