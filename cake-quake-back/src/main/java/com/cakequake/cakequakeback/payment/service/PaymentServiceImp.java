package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.payment.dto.*;
import com.cakequake.cakequakeback.payment.entities.Payment;
import com.cakequake.cakequakeback.payment.entities.PaymentStatus;
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
    private final CakeOrderItemRepository cakeOrderItemRepository;
    private final MemberRepository memberRepository;
    //private final TossPayService tossPayService;



    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequestDTO, Long userId) {
        //주문 엔티티 조회(없으면 예외)
        CakeOrderItem cakeOrderItem = cakeOrderItemRepository.findById(paymentRequestDTO.getOrderId())
                .orElseThrow(()-> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        //사용자 엔티티 조회 (없으면 예외)
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        //카카오 페이 준비(ready)호출
        KakaoPayReadyResponseDTO kakaoRes = kakaoPayService.ready(
                paymentRequestDTO.getShopId(), paymentRequestDTO.getOrderId(), paymentRequestDTO.getAmount()
        );

        //Payment 엔티티 생성 & 저장(Ready상태)
        Payment payment = Payment.builder()
                //.order(cakeOrderItem)
                .member(member)
                .provider(paymentRequestDTO.getProvider())
                .status(PaymentStatus.READY)
                .amount(paymentRequestDTO.getAmount())
                .transactionId(kakaoRes.getTid())
                .redirectUrl(kakaoRes.getNextRedirectPcRul())
                .paymentUrl(null)
                .build();
        paymentRepo.save(payment);



        return PaymentResponseDTO.fromEntity(payment);
    }

    //카카오페이 승인 콜백 처리
    @Override
    public PaymentResponseDTO approveKakao(String tid, String partnerOrderId, String PartnerUserId, String pgToken) {
        //tid로 기존 Payment 조회
        Payment payment = paymentRepo.findByTransactionId(tid)
                .orElseThrow(() -> new IllegalArgumentException("카카오 거래를 찾을 수 없습니다."));

        //상태 검증 (Ready여야 승인 가능)
        if(payment.getStatus() != PaymentStatus.READY){
            throw new IllegalStateException("승인할 수 없는 상태입니다");
        }

        //카카오페이 /v1/payment/approve 호출
        KakaoPayApproveResponse approveResponse = kakaoPayService.approvePayment(
                tid, partnerOrderId, PartnerUserId, pgToken
        );

        //응답 검증 (tid 일치 여부)
        if(approveResponse == null || !tid.equals(approveResponse.getTid())){
            throw new IllegalStateException("카카오페이 승인 응답이 유효하지 않습니다");
        }

        payment.approveByPg();
        paymentRepo.save(payment);

        return PaymentResponseDTO.fromEntity(payment);
    }

    //단건 결제 조화
    @Override
    @Transactional(readOnly = true)
    public PaymentResponseDTO getPayment(Long PaymentID, Long uid) {
        PaymentResponseDTO dto = paymentRepo.selectPaymentDTO(PaymentID, uid);
        if(dto == null){
            throw new IllegalArgumentException("결제 정보를 찾을 수 없습니다");
        }

        return dto;
    }

    //본인 결제 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponseDTO> listPayments(Long uid) {

        return paymentRepo.selectPaymentListDTO(uid);
    }

    //결제 취소
    @Override
    public PaymentResponseDTO cancelPayment(Long paymentId, Long uid, PaymentCancelRequestDTO paymentCancelRequestDTO) {
        Payment payment = paymentRepo.findByPaymentIdAndMemberUid(paymentId, uid)
                .orElseThrow(()-> new IllegalArgumentException("결제를 찾을 수 없습니다"));
        if(payment.getStatus() != PaymentStatus.APPROVED){
            throw new IllegalStateException("취소할 수 없는 상태입니다.");
        }

        //카카오페이 결제 취소 API 호출
        var cancelRes = kakaoPayService.cancel(
                payment.getOrder().getOrderId(),//.getShop().getShopId(), 로 바꿔야 함
                payment.getTransactionId(),
                payment.getAmount()
        );

        //응답 검증
        if(cancelRes == null || !"CANCEL".equalsIgnoreCase(cancelRes.getStatus())){
            throw new IllegalStateException("카카오페이 결제 취소 실패");
        }

        //엔티티 상태 변경 & 저장
        payment.cancelByBuyer(paymentCancelRequestDTO.getReason());
        paymentRepo.save(payment);


        return paymentRepo.selectPaymentDTO(paymentId,uid);
    }

    //결제 환불
    @Override
    public PaymentResponseDTO refundPayment(Long paymentId, Long uid, PaymentRefundRequestDTO paymentRefundRequestDTO) {

        Payment payment = paymentRepo.findByPaymentIdAndMemberUid(paymentId,uid)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다."));

        PaymentStatus status = payment.getStatus();
        if(status != PaymentStatus.APPROVED && status != PaymentStatus.CANCELLED){
            throw new IllegalStateException("환불할 수 없는 상태입니다.");
        }

        var cancelRes = kakaoPayService.cancel(
                payment.getOrder().getOrderId(),//.getShop().getShopId(), 로 바꿔야 함
                payment.getTransactionId(),
                payment.getAmount()
        );

        //응답 검증
         if(cancelRes == null || !"CANCEL".equalsIgnoreCase(cancelRes.getStatus())){
             throw new IllegalStateException("카카오페이 환불 실패");
         }

         payment.refundByBuyer(paymentRefundRequestDTO.getReason());
         paymentRepo.save(payment);

        return paymentRepo.selectPaymentDTO(paymentId,uid);
    }
}
