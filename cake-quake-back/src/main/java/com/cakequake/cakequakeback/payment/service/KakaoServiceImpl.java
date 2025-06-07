package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.common.config.AppConfig;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.repo.BuyerOrderRepository;
import com.cakequake.cakequakeback.payment.dto.KakaoPayApproveResponse;
import com.cakequake.cakequakeback.payment.dto.KakaoPayCancelResponseDTO;
import com.cakequake.cakequakeback.payment.dto.KakaoPayReadyResponseDTO;
import com.cakequake.cakequakeback.payment.entities.MerchantPaymentKey;
import com.cakequake.cakequakeback.payment.entities.Payment;
import com.cakequake.cakequakeback.payment.entities.PaymentProvider;
import com.cakequake.cakequakeback.payment.entities.PaymentStatus;
import com.cakequake.cakequakeback.payment.repo.MerchantPaymentRepo;
import com.cakequake.cakequakeback.payment.repo.PaymentRepo;
import com.cakequake.cakequakeback.payment.sercurity.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static com.cakequake.cakequakeback.payment.entities.QPayment.payment;

@Service
@RequiredArgsConstructor
@Transactional
public class KakaoServiceImpl implements KakaoPayService {

    //RestTemplate를 이용해 카카오페이 REST API를 호출
    private final AppConfig appConfig;
    private final MerchantPaymentRepo merchantPaymentRepo;
    private final EncryptionService encryptionService;
    private final BuyerOrderRepository buyerOrderRepository;
    private final PaymentRepo paymentRepo;

    @Value("${spring.pg.kakao.base-url}")
    private String baseUrl;

    @Value("${spring.app.base-url}")


    private String appBaseUrl;

    //카카오페이 결제 준비 요청
    @Override
    public KakaoPayReadyResponseDTO ready( Long orderId,Long userId, BigDecimal amount) {
        //주문 조회
        CakeOrder order = buyerOrderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId=" + orderId));
        Long shopId = order.getShop().getShopId();

        //MerchantPaymentKey 조회 -> 복호화
        MerchantPaymentKey key = merchantPaymentRepo.findByShopIdAndProviderAndIsActive( shopId,"KAKAO",true)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 카카오페이 키가 없습니다"));


        String cid  = encryptionService.decrypt(key.getEncryptedApiKey());
        String adminKey = encryptionService.decrypt(key.getEncryptedSecret());


        System.out.println(">>> [DEBUG] 복호화된 AdminKey = " + adminKey);
        System.out.println(">>> [DEBUG] 복호화된 CID      = " + cid);

        // 2) kakaoBaseUrl이 실제로 주입되었는지 로그로 확인
        System.out.println(">>> [DEBUG] kakaoBaseUrl = " + baseUrl);
        // (콘솔에 ">>> [DEBUG] kakaoBaseUrl = https://kapi.kakao.com" 와 같이 찍혀야 합니다.)

        //HTTP 헤더 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + adminKey);

        // ◀ 여기가 실제로 어떤 Header가 달려 있는지 찍어보기
        System.out.println(">>> [OUTGOING] Authorization 헤더 = '" + headers.getFirst("Authorization") + "'");

//        // 4) Payment 엔티티를 미리 저장하여 paymentId 획득 (transactionId는 빈 문자열로 초기 저장)
//        Payment pendingPayment = Payment.builder()
//                .order(order)
//                .member(order.getMember())
//                .provider(PaymentProvider.KAKAO)
//                .status(PaymentStatus.READY)
//                .amount(amount)
//                .transactionId("")   // 우선 빈 문자열로 저장
//                .redirectUrl(null)
//                .paymentUrl(null)
//                .build();

//        Payment savedPayment = paymentRepo.save(pendingPayment);
//        Long paymentId = savedPayment.getPaymentId();

//        String approvalUrl = appBaseUrl
//                + "/api/payments/kakao/approve"
//                + "?partner_order_id=" + orderId
//                + "&partner_user_id=" + userId;

        String approvalUrl = appBaseUrl
                + "/api/payments/kakao/approve"
                + "?partner_order_id=" + orderId
                + "&partner_user_id=" + userId;
                //+"&pg_token={pg_token}";
        String cancelUrl   = appBaseUrl + "/api/payments/kakao/cancel";
        String failUrl     = appBaseUrl + "/api/payments/kakao/fail";

        // 4) ▼ 여기를 Map → MultiValueMap 으로 변경
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("partner_order_id", orderId.toString());
        params.add("partner_user_id", userId.toString());
        params.add("item_name",     "CakeOrder#" + orderId);
        params.add("quantity",      "1");
        params.add("total_amount",  amount.toString());
        params.add("tax_free_amount", "0");
        params.add("approval_url",   approvalUrl);
        params.add("cancel_url",     cancelUrl);
        params.add("fail_url",       failUrl);

        // HttpEntity에 MultiValueMap을 담기
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);


        KakaoPayReadyResponseDTO response = appConfig.restTemplate().postForObject(
                baseUrl + "/v1/payment/ready",
                requestEntity,
                KakaoPayReadyResponseDTO.class
        );
        System.out.println(">>> [DEBUG] kakao ready response = " + response);

        if (response == null || response.getTid() == null) {
            throw new IllegalStateException("카카오페이 결제 준비 요청에 실패했습니다.");
        }


        return response;
    }

    //결제 요청 취소 요청
    @Override
    public KakaoPayCancelResponseDTO cancel(Long paymentId) {

        Payment payment = paymentRepo.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 결제를 찾을 수 없습니다"));

        Long shopId = payment.getOrder().getShop().getShopId();
        String tid = payment.getTransactionId();
        BigDecimal amount = payment.getAmount();


        //MerchantPaymentKey 조회 -> 복호화
        MerchantPaymentKey key = merchantPaymentRepo.findByShopIdAndProviderAndIsActive(shopId, "KAKAO",true)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 카카오페이 키가 없습니다"));

        String cid = encryptionService.decrypt(key.getEncryptedApiKey());
        String adminKey = encryptionService.decrypt(key.getEncryptedSecret());

        //HTTP 헤더 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + adminKey);

        String cancelAmount = amount.longValue() +"";
        String cancelTaxFreeAmt = "0";

        //요청 파라미터(from-data) 구성
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("tid", tid);
        params.add("cancel_amount", cancelAmount);
        params.add("cancel_tax_free_amount", cancelTaxFreeAmt);

        HttpEntity<MultiValueMap<String,String>> requestEntity = new HttpEntity<>(params, headers);
//        KakaoPayCancelResponseDTO response = appConfig.restTemplate().postForObject(
//                baseUrl + "/v1/payment/cancel",
//                requestEntity,
//                KakaoPayCancelResponseDTO.class
//        );

        KakaoPayCancelResponseDTO response;
        try {
            // 5) 카카오 /v1/payment/cancel 호출
            response = appConfig.restTemplate().postForObject(
                    baseUrl + "/v1/payment/cancel",
                    requestEntity,
                    KakaoPayCancelResponseDTO.class
            );

            // ★ 정상 리턴된 경우에도 response.status를 찍어 봅니다.
            System.out.println(">>> [DEBUG] KakaoPay cancel 정상 응답, response.status=" +
                    (response != null ? response.getStatus() : "NULL"));
        }
        catch (HttpClientErrorException e) {
            String body = e.getResponseBodyAsString();
            System.out.println(">>> [DEBUG] cancel 오류 body: " + body);

            // 6) “이미 전액 취소됨” 오류 처리 (code=-710)
            if (body != null && body.contains("\"code\":-710")) {
                // 이미 취소된 상태이므로, 우린 성공 처리를 해준다.
                KakaoPayCancelResponseDTO fake = new KakaoPayCancelResponseDTO();
                fake.setStatus("CANCELLED");
                System.out.println(">>> [DEBUG] code=-710 감지, 인위적으로 status=CANCEL 세팅");
                return fake;
            }

            // 7) 그 외 파라미터 오류(code=-2)라면 명확한 메시지 던지기
            if (body != null && body.contains("\"code\":-2")) {
                throw new IllegalArgumentException(
                        "카카오파라미터 오류: cancel_amount 등 입력값을 확인하세요. 응답=" + body
                );
            }
            // 8) 그 외에는 “완전 실패” 예외 던져버리기
            throw new IllegalStateException("카카오페이 결제 취소 요청이 실패했습니다: " + body, e);
        }
//
//        if(response == null || !"CANCEL".equalsIgnoreCase(response.getStatus())){
//            throw  new IllegalStateException("카카오페이 결제 취소 요청이 실패했습니다.");
//        }

        // 기존: !"CANCEL".equalsIgnoreCase(response.getStatus())
        // 수정: "CANCEL", "CANCEL_PAYMENT" 등을 모두 허용
        String status = response.getStatus();
        if (!( "CANCEL".equalsIgnoreCase(status)
                || "CANCEL_PAYMENT".equalsIgnoreCase(status)
                || "CANCELED".equalsIgnoreCase(status) )) {
            System.out.println(">>> [DEBUG] response.getStatus() 가 취소 성공 상태가 아닙니다. 실제 status=" + status);
            throw new IllegalStateException(
                    "카카오페이 결제 취소 요청이 실패했습니다. (status=" + status + ")");
        }

        return response;
    }

    //카카오페이 결제 승인 API 호출
    @Override
    public KakaoPayApproveResponse approvePayment(String tid, String partnerOrderId, String partnerUserId, String pgToken) {
//        Long shopId = Long.valueOf(partnerUserId); //partenerUserId로 shopId쓰는 경우
//        MerchantPaymentKey key =merchantPaymentRepo.findByShopIdAndProviderAndIsActive(shopId,"KAKAO",true)
//                .orElseThrow(()->
//                        new IllegalArgumentException("활성화된 카카오페이 키가 없습니다."));

        Payment payment = paymentRepo.findByTransactionId(tid)
                .orElseThrow(() ->
                        new IllegalArgumentException("카카오페이 거래가 존재하지 않습니다."));
        Long shopId = payment.getOrder().getShop().getShopId();

        // 3) 활성화된 MerchantPaymentKey를 shopId + “KAKAO”로 조회
        MerchantPaymentKey key = merchantPaymentRepo
                .findByShopIdAndProviderAndIsActive(shopId, "KAKAO", true)
                .orElseThrow(() ->
                        new IllegalArgumentException("활성화된 카카오페이 키가 없습니다.")
                );


        String cid = encryptionService.decrypt(key.getEncryptedApiKey());
        String adminKey = encryptionService.decrypt(key.getEncryptedSecret());

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK "  + adminKey);

        //  요청 파라미터(form-data) 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("tid", tid);
        params.add("partner_order_id", partnerOrderId);
        params.add("partner_user_id", partnerUserId);
        params.add("pg_token", pgToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity =
                new HttpEntity<>(params, headers);
        KakaoPayApproveResponse response = appConfig.restTemplate().postForObject(
                baseUrl + "/v1/payment/approve",
                requestEntity,
                KakaoPayApproveResponse.class
        );
        if(response == null || response.getTid() == null){
            throw new IllegalStateException("카카오페이 결제 승인 요청에 실패했습니다");
        }
        return response;
    }


}

