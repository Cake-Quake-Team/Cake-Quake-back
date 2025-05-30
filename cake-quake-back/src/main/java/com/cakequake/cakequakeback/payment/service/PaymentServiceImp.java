package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.payment.dto.PaymentCancelRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRefundRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO;
import com.cakequake.cakequakeback.payment.repo.PaymentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImp implements PaymentService{

    private final PaymentRepo paymentRepo;
    private final KakaoPayService kakaoPayService;
    private final TossPayService tossPayService;

    @Override
    public PaymentRequestDTO createPayment(PaymentRequestDTO paymentRequestDTO, Long uid) {
        return null;
    }

    @Override
    public PaymentRequestDTO getPayment(Long PaymentID, Long uid) {
        return null;
    }

    @Override
    public List<PaymentResponseDTO> listPayments(Long uid) {
        return List.of();
    }

    @Override
    public PaymentResponseDTO cancelPayment(Long paymentID, Long uid, PaymentCancelRequestDTO PaymentCancelRequestDTO) {
        return null;
    }

    @Override
    public PaymentResponseDTO refunPayment(Long paymentID, Long uid, PaymentRefundRequestDTO PaymentRefundRequestDTO) {
        return null;
    }
}
