package com.cakequake.cakequakeback.payment.controller;

import com.cakequake.cakequakeback.payment.dto.PaymentCancelRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRefundRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentRequestDTO;
import com.cakequake.cakequakeback.payment.dto.PaymentResponseDTO;
import com.cakequake.cakequakeback.payment.service.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.util.MultiValueMap;
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


        // 2) partner_order_id / partner_user_id가 null인 경우
        //    예를 들어 카카오가 쿼리스트링을 생략했다면, DB 등 다른 방법으로 찾아야 한다.
        if (orderId == null || userId == null) {
            // DB에 저장된 Payment 목록에서 "PG 토큰만으로 결제 준비 정보 찾기"는
            // 카카오 API 특성상 직접 불가능합니다. 대신 orderId/userId를 approval_url에
            // 반드시 붙여 보내야 방어가 쉽습니다.
            throw new IllegalArgumentException(
                    "필수 파라미터(partner_order_id, partner_user_id)가 누락되었습니다."
            );
        }
        // 서비스에 orderId, userId, pgToken만 넘겨서
        // 내부에서 tid 조회 및 승인 절차를 모두 처리하게 한다.
        return paymentService.approveKakao(orderId, userId, pgToken);
    }


    @GetMapping("/toss/success")
    public ResponseEntity<PaymentResponseDTO> tossSuccess(
            @RequestParam("paymentKey") String paymentKey,
            @RequestParam("orderId")   String orderIdStr
    ) {
        // 2) 서비스에 위 파라미터 넘겨서 승인 처리
        PaymentResponseDTO dto = paymentService.approveToss(paymentKey, orderIdStr);
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
