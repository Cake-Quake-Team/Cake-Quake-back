package com.cakequake.cakequakeback.payment.controller;

import com.cakequake.cakequakeback.payment.dto.PaymentCancelRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRefundRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO;
import com.cakequake.cakequakeback.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;


    //결제 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDTO create(
            @Valid @RequestBody PaymentRequestDTO paymentRequestDTO
            //,@AuthenticationPrincipal Long uid
            ){
        Long uid = 1L;
        return paymentService.createPayment(paymentRequestDTO, uid);
    }

    //카카오페이 승인 콜백
    @GetMapping("/kakao/approve")
    public PaymentResponseDTO approveKakao(
            @RequestParam String tid,
            @RequestParam("partner_order_id") String partnerOrderId,
            @RequestParam("partner_user_id") String partnerUserId,
            @RequestParam("pg_token") String pgToken
    ){
        return paymentService.approveKakao(tid, partnerOrderId, partnerUserId, pgToken);
    }

    //단건 조회
    @GetMapping("/{paymentId}")
    public PaymentResponseDTO getPayment(
        @PathVariable Long paymentId,
        @AuthenticationPrincipal Long uid
    ){
        return paymentService.getPayment(paymentId, uid);
    }

    //본인 결제 목록 조회
    @GetMapping
    public List<PaymentResponseDTO> listPayments(
            @AuthenticationPrincipal Long uid
    ){
        return paymentService.listPayments(uid);
    }

    //결제 취소
    @PostMapping("/{paymentId}/cancel")
    public PaymentResponseDTO cancelPayment(
            @PathVariable Long paymentId,
            //@AuthenticationPrincipal Long uid,
            @Valid @RequestBody PaymentCancelRequestDTO dto
    ) {
        Long uid = 1L;
        return paymentService.cancelPayment(paymentId, uid, dto);
    }

    //결제 환불
    @PostMapping("/{paymentId}/refund")
    public PaymentResponseDTO refundPayment(
            @PathVariable Long paymentId,
            //@AuthenticationPrincipal Long uid,
            @Valid @RequestBody PaymentRefundRequestDTO dto
    ) {
        Long uid = 1L;
        return paymentService.refundPayment(paymentId, uid, dto);
    }



}
