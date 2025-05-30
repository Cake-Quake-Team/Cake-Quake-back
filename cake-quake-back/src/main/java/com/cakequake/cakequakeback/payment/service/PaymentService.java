package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.payment.dto.PaymentCancelRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRefundRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO;

import java.util.List;

public interface PaymentService {

    //결제 요청
    PaymentRequestDTO createPayment(PaymentRequestDTO paymentRequestDTO, Long uid);

    // 본인 결제 단건 조회
    PaymentRequestDTO getPayment(Long PaymentID,Long uid);

    //내 결제 내역 전체 조회
    List<PaymentResponseDTO> listPayments(Long uid);

    //구매자 취소
    PaymentResponseDTO cancelPayment(Long paymentID, Long uid, PaymentCancelRequestDTO PaymentCancelRequestDTO);

    //환불 요청
    PaymentResponseDTO refunPayment(Long paymentID, Long uid, PaymentRefundRequestDTO PaymentRefundRequestDTO);
}



