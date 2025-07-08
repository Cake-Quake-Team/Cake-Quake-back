package com.cakequake.cakequakeback.payment.controller;

import com.cakequake.cakequakeback.payment.dto.PaymentCancelRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRefundRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO;
import com.cakequake.cakequakeback.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Log4j2
public class PaymentController {

    private final PaymentService paymentService;


    //결제 생성
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponseDTO create(
            @Valid @RequestBody PaymentRequestDTO paymentRequestDTO
            , @AuthenticationPrincipal(expression = "member.uid") Long uid
            ){
        return paymentService.createPayment(paymentRequestDTO, uid);
    }


    @GetMapping("/kakao/approve")
    public PaymentResponseDTO approveKakao(
            @RequestParam("partner_order_id") Long orderId,
            @RequestParam("partner_user_id")  Long userId,
            @RequestParam("pg_token")        String pgToken,
            HttpServletRequest request
    ) {

        if (orderId == null || userId == null) {

            throw new IllegalArgumentException(
                    "필수 파라미터(partner_order_id, partner_user_id)가 누락되었습니다."
            );
        }

        return paymentService.approveKakao(orderId, userId, pgToken);
    }


    @GetMapping("/toss/success")
    public ResponseEntity<PaymentResponseDTO> tossSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId")   String orderIdStr
    ) {
        log.debug("▶▶▶ tossSuccess 콜백 탐! paymentKey={}, orderId={}", paymentKey, orderIdStr);
        PaymentResponseDTO dto = paymentService.approveToss(paymentKey, orderIdStr);
        log.debug("▶▶▶ approveToss 리턴 DTO: {}", dto);
        return ResponseEntity.ok(dto);
    }

    /** 토스페이 결제 실패 콜백 */
    @GetMapping("/toss/fail")
    public ResponseEntity<String> tossFail(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId")   String orderIdStr,
            @RequestParam(value="errorCode",    required=false) String errorCode,
            @RequestParam(value="errorMessage", required=false) String errorMessage
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("결제 실패: " + errorMessage + " (code=" + errorCode + ")");
    }


    //단건 조회
    @GetMapping("/{paymentId}")
    public PaymentResponseDTO getPayment(
        @PathVariable Long paymentId,
        @AuthenticationPrincipal(expression = "member.uid") Long uid
    ){

        return paymentService.getPayment(paymentId, uid);
    }

    //본인 결제 목록 조회
    @GetMapping
    public List<PaymentResponseDTO> listPayments(
            @AuthenticationPrincipal(expression = "member.uid") Long uid
    ){
        return paymentService.listPayments(uid);
    }

    //주문결 결제 목록 조회
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<PaymentResponseDTO>> orderPayments(
            @PathVariable Long orderId,
            @AuthenticationPrincipal(expression = "member.uid") Long uid
    ) {
        List<PaymentResponseDTO> list = paymentService.listOrderPayments(orderId, uid);
        return ResponseEntity.ok(list);
    }

    //결제 취소
    @PostMapping("/{paymentId}/cancel")
    public PaymentResponseDTO cancelPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal(expression = "member.uid")  Long uid,
            @Valid @RequestBody PaymentCancelRequestDTO dto
    ) {
        return paymentService.cancelPayment(paymentId, uid, dto);
    }

    //결제 환불
    @PostMapping("/{paymentId}/refund")
    public PaymentResponseDTO refundPayment(
            @PathVariable Long paymentId,
            @AuthenticationPrincipal(expression = "member.uid") Long uid,
            @Valid @RequestBody PaymentRefundRequestDTO dto
    ) {

        return paymentService.refundPayment(paymentId, uid, dto);
    }
}
