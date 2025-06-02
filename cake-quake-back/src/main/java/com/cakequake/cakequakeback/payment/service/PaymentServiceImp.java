package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.payment.dto.*;
import com.cakequake.cakequakeback.payment.entities.Payment;
import com.cakequake.cakequakeback.payment.entities.PaymentProvider;
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
    private final TossPayService tossPayService;



    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequestDTO, Long userId) {
        //주문 엔티티 조회(없으면 예외)
        CakeOrderItem cakeOrderItem = cakeOrderItemRepository.findById(paymentRequestDTO.getOrderId())
                .orElseThrow(()-> new IllegalArgumentException("주문을 찾을 수 없습니다."));
        //사용자 엔티티 조회 (없으면 예외)
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        //provider 분기("KAKAO", "TOSS")
        PaymentProvider proveider = paymentRequestDTO.getProvider();
        Payment paymentEntity;

        if(proveider == PaymentProvider.KAKAO){
            //카카오 페이 준비(ready)호출
            KakaoPayReadyResponseDTO kakaoRes = kakaoPayService.ready(
                    paymentRequestDTO.getShopId(), paymentRequestDTO.getOrderId(), paymentRequestDTO.getAmount()
            );

            //Payment 엔티티 생성 & 저장(Ready상태)
            paymentEntity = Payment.builder()
                    //.order(cakeOrderItem)
                    .member(member)
                    .provider(paymentRequestDTO.getProvider())
                    .status(PaymentStatus.READY)
                    .amount(paymentRequestDTO.getAmount())
                    .transactionId(kakaoRes.getTid())
                    .redirectUrl(kakaoRes.getNextRedirectPcRul())
                    .paymentUrl(null)
                    .build();
        }
        else if(proveider == PaymentProvider.TOSS){
            //토스페이 준비
            String customerKey = "USER_" + userId;

            TossPayReadyResponseDTO tossResponse = tossPayService.requestPayment(
                    paymentRequestDTO.getShopId(),
                    paymentRequestDTO.getOrderId(),
                    customerKey,
                    paymentRequestDTO.getAmount().longValue()
            );

            paymentEntity = Payment.builder()
                    //.order(cakeOrderItem)
                    .member(member)
                    .provider(PaymentProvider.TOSS)
                    .status(PaymentStatus.READY)
                    .amount(paymentRequestDTO.getAmount())
                    .transactionId(tossResponse.getPaymentKey())
                    .redirectUrl(null)  //카카오처럼 Redirect용이 없으므로 null
                    .paymentUrl(tossResponse.getPaymentUrl())
                    .build();
        }
        else{
            throw new IllegalArgumentException("지원하지 않는 결제 수단입니다");
        }

        Payment saved = paymentRepo.save(paymentEntity);

        return PaymentResponseDTO.fromEntity(saved);
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
        Payment update = paymentRepo.save(payment);

        return PaymentResponseDTO.fromEntity(update);
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

        //본인 결제 엔티티 조회
        Payment payment = paymentRepo.findByPaymentIdAndMemberUid(paymentId, uid)
                .orElseThrow(()-> new IllegalArgumentException("결제를 찾을 수 없습니다"));
        //상태 검증
        if(payment.getStatus() != PaymentStatus.APPROVED){
            throw new IllegalStateException("취소할 수 없는 상태입니다.");
        }

        PaymentProvider provider = payment.getProvider();


        if(provider == PaymentProvider.KAKAO){
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

        }
        else if(provider == PaymentProvider.TOSS){

            TossPayCancelRequestDTO cancelRequest = TossPayCancelRequestDTO.builder()
                    .paymentKey(payment.getTransactionId())
                    .amount(payment.getAmount().longValue())
                    .reason(paymentCancelRequestDTO.getReason())
                    .build();

            TossPayCancelResponseDTO tossCancelResponse = tossPayService.cancel(
                payment.getOrder().getOrderId(), //마찬가지로 shopIㅇ로 바꾸려면 구조 수정 필요
                cancelRequest
            );

            //응답 검증(토스페이 "status"필드가 "CANCELED"인지 확인)
            if(tossCancelResponse == null || !"CANCEL".equalsIgnoreCase(tossCancelResponse.getStatus())){
                throw new IllegalStateException("토스페이 환불 요청 실패");
            }

            // 엔티티 상태 변경 및 저장 : CANCELED
            payment.refundByBuyer(paymentCancelRequestDTO.getReason());
            paymentRepo.save(payment);
        }
        else {
            throw new IllegalArgumentException("지원하지 않는 결제 수단입니다.");
        }

        return paymentRepo.selectPaymentDTO(paymentId,uid);
    }

    //결제 환불 : 이미 취소됐거나 완료된 결제에 대해 고객에게 실제로 금액을 돌려줄 때
    @Override
    public PaymentResponseDTO refundPayment(Long paymentId, Long uid, PaymentRefundRequestDTO paymentRefundRequestDTO) {
        //본인 결제 엔티티 조회
        Payment payment = paymentRepo.findByPaymentIdAndMemberUid(paymentId,uid)
                .orElseThrow(() -> new IllegalArgumentException("결제를 찾을 수 없습니다."));

        //상태 검증 이미 완료이거나, 이미 취소된 상태에서만 가능
        PaymentStatus status = payment.getStatus();
        if(status != PaymentStatus.APPROVED && status != PaymentStatus.CANCELLED){
            throw new IllegalStateException("환불할 수 없는 상태입니다.");
        }

        PaymentProvider provider = payment.getProvider();

        if(provider == PaymentProvider.KAKAO){
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

        }
        else if(provider == PaymentProvider.TOSS){
            //토스페이 환불 요청 DTO생성
            TossPayRefundRequestDTO refundRequestDTO = TossPayRefundRequestDTO.builder()
                    .paymentKey(payment.getTransactionId())
                    .amount(payment.getAmount().longValue())
                    .reason(paymentRefundRequestDTO.getReason())
                    .build();
            // 토스페이 refund API호출
            TossPayRefundResponseDTO tossRefundRespons = tossPayService.refund(
                    payment.getOrder().getOrderId(), //실제 shop으로 바꿀 필요있음
                    refundRequestDTO
            );

            if(tossRefundRespons == null || !"DONE".equalsIgnoreCase(tossRefundRespons.getStatus())){
                throw new IllegalStateException("토스페이 환불 요청 실패");
            }

            payment.refundByBuyer(paymentRefundRequestDTO.getReason());
            paymentRepo.save(payment);
        }else{
            throw new IllegalArgumentException("지원하지 않는 결제 수단입니다");
        }



        return paymentRepo.selectPaymentDTO(paymentId,uid);
    }
}
