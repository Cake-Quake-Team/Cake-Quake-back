package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.payment.dto.*;
import com.cakequake.cakequakeback.payment.entities.MerchantPaymentKey;
import com.cakequake.cakequakeback.payment.entities.Payment;
import com.cakequake.cakequakeback.payment.entities.PaymentProvider;
import com.cakequake.cakequakeback.payment.entities.PaymentStatus;
import com.cakequake.cakequakeback.payment.repo.MerchantPaymentRepo;
import com.cakequake.cakequakeback.payment.repo.PaymentRepo;
import com.cakequake.cakequakeback.payment.sercurity.EncryptionService;
import com.cakequake.cakequakeback.point.service.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.token.TokenService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService{

    private final PaymentRepo paymentRepo;
    private final KakaoPayService kakaoPayService;
    private final BuyerOrderRepository buyerOrderRepository;
    private final MemberRepository memberRepository;
    private final TossPayService tossPayService;
    private final PointService pointService;


    @Override
    public PaymentResponseDTO createPayment(PaymentRequestDTO paymentRequestDTO, Long uid) {
        //주문 엔티티 조회(없으면 예외)
        CakeOrder cakeOrder = buyerOrderRepository.findByOrderIdAndMemberUid(
                paymentRequestDTO.getOrderId(),
                uid
        ).orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없거나 권한이 없습니다."));
        //사용자 엔티티 조회 (없으면 예외)
        Member member = memberRepository.findById(uid)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));

        //provider 분기("KAKAO", "TOSS")
        PaymentProvider provider = paymentRequestDTO.getProvider();
        Payment paymentEntity;

        if(provider == PaymentProvider.KAKAO){
            //카카오 페이 준비(ready)호출
            KakaoPayReadyResponseDTO kakaoRes = kakaoPayService.ready(
                    paymentRequestDTO.getOrderId(),
                    member.getUid(),
                    paymentRequestDTO.getAmount()
            );

            //Payment 엔티티 생성 & 저장(Ready상태)
            paymentEntity = Payment.builder()
                    .order(cakeOrder)
                    .member(member)
                    .provider(paymentRequestDTO.getProvider())
                    .status(PaymentStatus.READY)
                    .amount(paymentRequestDTO.getAmount())
                    .transactionId(kakaoRes.getTid())
                    .redirectUrl(kakaoRes.getNextRedirectPcRul())
                    .paymentUrl(null)
                    .build();
        }


        else if(provider == PaymentProvider.TOSS){
            //토스페이 준비
            String customerKey = "USER_" + uid;

            TossPayReadyResponseDTO tossResponse = tossPayService.ready(
                    paymentRequestDTO.getOrderId(),
                    member.getUid(),
                    customerKey,
                    paymentRequestDTO.getAmount().longValue()
            );

            paymentEntity = Payment.builder()
                    .order(cakeOrder)
                    .member(member)
                    .provider(PaymentProvider.TOSS)
                    .status(PaymentStatus.READY)
                    .amount(paymentRequestDTO.getAmount())
                    .transactionId(tossResponse.getPaymentKey())
                    .redirectUrl(null)  //카카오처럼 Redirect용이 없으므로 null
                    .paymentUrl(null)
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
    public PaymentResponseDTO approveKakao( Long orderId, Long userId, String pgToken) {
        //tid로 기존 Payment 조회
        Payment payment = paymentRepo.findOneByOrderOrderIdAndMemberUidAndStatus(orderId, userId, PaymentStatus.READY)
                .orElseThrow(() -> new IllegalArgumentException("카카오 거래를 찾을 수 없습니다."));

        //상태 검증 (Ready여야 승인 가능)
        if(payment.getStatus() != PaymentStatus.READY){
            throw new IllegalStateException("승인할 수 없는 상태입니다");
        }
        String tid = payment.getTransactionId();
        if (tid == null) {
            throw new IllegalStateException(
                    "결제 준비 단계에서 저장된 tid가 없습니다."
            );
        }


        // 4) partner_order_id, partner_user_id 를 문자열로 변환
        String partnerOrderId = payment.getOrder().getOrderId().toString();
        String partnerUserId  = payment.getMember().getUid().toString();

        //카카오페이 /v1/payment/approve 호출
        KakaoPayApproveResponse approveResponse = kakaoPayService.approvePayment(
                tid, partnerOrderId, partnerUserId, pgToken
        );

        //응답 검증 (tid 일치 여부)
        if(approveResponse == null || !tid.equals(approveResponse.getTid())){
            throw new IllegalStateException("카카오페이 승인 응답이 유효하지 않습니다");
        }

        payment.approveByPg();
        Payment update = paymentRepo.save(payment);


        // 포인트 적립: 결제 금액의 1% 적립
        long pointsToAdd = payment.getAmount().longValue() / 100;
        pointService.changePoint(userId, pointsToAdd,
                "결제 포인트 적립(1%): 주문 #" + orderId);

        return PaymentResponseDTO.fromEntity(update);
    }


    @Override
    public PaymentResponseDTO approveToss(String paymentKey, String orderIdStr) {
        // 1) paymentKey 로 결제 찾기
        Payment payment = paymentRepo.findByTransactionId(paymentKey)
                .orElseThrow(() -> new IllegalArgumentException("결제 정보를 찾을 수 없습니다"));

        // 2) 승인 가능한 상태인지 검증
        if (payment.getStatus() != PaymentStatus.READY) {
            throw new IllegalStateException("승인할 수 없는 상태입니다: " + payment.getStatus());
        }

        // 3) 엔티티 상태 변경
        payment.approveByPg();
        paymentRepo.save(payment);

        // 포인트 적립: 결제 금액의 1% 적립
        Long uid = payment.getMember().getUid();
        long pointsToAdd = payment.getAmount().longValue() / 100;
        pointService.changePoint(uid, pointsToAdd,
                "결제 포인트 적립(1%): paymentKey=" + paymentKey);

        // 4) DTO 반환
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

        System.out.println(">>> cancelPayment 진입! paymentId=" + paymentId + ", reason=" + paymentCancelRequestDTO.getReason());
        //본인 결제 엔티티 조회
        Payment payment = paymentRepo.findByPaymentIdAndMemberUid(paymentId, uid)
                .orElseThrow(()-> new IllegalArgumentException("결제를 찾을 수 없습니다"));

        System.out.println(">>> payment.getStatus() = " + payment.getStatus());

        //상태 검증
        if(payment.getStatus() != PaymentStatus.APPROVED){
            throw new IllegalStateException("취소할 수 없는 상태입니다.");
        }

        PaymentProvider provider = payment.getProvider();


        if(provider == PaymentProvider.KAKAO){
            System.out.println(">>> 카카오페이 취소 API 호출 직전: tid=" + payment.getTransactionId()
                    + ", amount=" + payment.getAmount());

            //카카오페이 결제 취소 API 호출
            KakaoPayCancelResponseDTO cancelRes = kakaoPayService.cancel(paymentId);

//            //응답 검증
//            if(cancelRes == null || !"CANCEL".equalsIgnoreCase(cancelRes.getStatus())){
//                throw new IllegalStateException("카카오페이 결제 취소 실패");
//            }

            //엔티티 상태 변경 & 저장
//            payment.cancelByBuyer(paymentCancelRequestDTO.getReason());
//            paymentRepo.save(payment);

            String status = cancelRes != null ? cancelRes.getStatus() : null;
            if (status != null &&
                    ( "CANCEL".equalsIgnoreCase(status)
                            || "CANCEL_PAYMENT".equalsIgnoreCase(status)
                            || "CANCELED".equalsIgnoreCase(status) )) {

                // 엔티티 상태 변경 & 저장
                payment.cancelByBuyer(paymentCancelRequestDTO.getReason());
                paymentRepo.save(payment);
                System.out.println(">>> DB에 status=CANCELLED 로 저장완료");
            }
            System.out.println(">>> DB에 status=CANCELLED 로 저장완료");
        }


        else if(provider == PaymentProvider.TOSS){

            TossPayCancelRequestDTO cancelRequest = TossPayCancelRequestDTO.builder()
                    //.paymentKey(payment.getTransactionId())
                    .cancelAmount(payment.getAmount().longValue())
                    .cancelReason(paymentCancelRequestDTO.getReason())
                    .build();

            TossPayCancelResponseDTO tossCancelResponse = tossPayService.cancel(
                payment.getOrder().getShop().getShopId(), //마찬가지로 shopIㅇ로 바꾸려면 구조 수정 필요
                payment.getTransactionId(),
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

        // 포인트 차감: 결제 금액의 1% 차감
        long pointsToDeduct = payment.getAmount().longValue() / 100;
        pointService.changePoint(uid, -pointsToDeduct,
                "환불 포인트 차감(1%): paymentId=" + paymentId);

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
            // 카카오페이에서는 '환불'도 결국 Cancel API를 사용하므로, 동일한 cancel 메서드를 호출합니다.
            KakaoPayCancelResponseDTO cancelRes = kakaoPayService.cancel(paymentId);

            // 응답 검증: 여러 케이스의 취소 완료 상태를 허용
            String resStatus = (cancelRes != null ? cancelRes.getStatus() : null);
            if (resStatus == null ||
                    !( "CANCEL".equalsIgnoreCase(resStatus)
                            || "CANCEL_PAYMENT".equalsIgnoreCase(resStatus)
                            || "CANCELED".equalsIgnoreCase(resStatus) )) {
                System.out.println(">>> [ERROR] cancelRes.getStatus() 가 환불(취소) 완료 상태가 아닙니다. 실제 status=" + resStatus);
                throw new IllegalStateException("카카오페이 환불(취소) 실패. (status=" + resStatus + ")");
            }

            // 엔티티 상태 변경 & 저장 (refundByBuyer 메서드는 상태를 REFUNDED로 변경)
            payment.refundByBuyer(paymentRefundRequestDTO.getReason());
            paymentRepo.save(payment);
            System.out.println(">>> DB에 status=REFUNDED 로 저장완료");

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


        // 포인트 차감: 결제 금액의 1% 차감
        long pointsToDeduct = payment.getAmount().longValue() / 100;
        pointService.changePoint(uid, -pointsToDeduct,
                "환불 포인트 차감(1%): paymentId=" + paymentId);

        return paymentRepo.selectPaymentDTO(paymentId,uid);
    }
}
