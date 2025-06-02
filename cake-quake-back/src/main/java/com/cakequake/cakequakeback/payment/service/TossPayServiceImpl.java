package com.cakequake.cakequakeback.payment.service;


import com.cakequake.cakequakeback.common.config.AppConfig;
import com.cakequake.cakequakeback.payment.dto.*;
import com.cakequake.cakequakeback.payment.entities.MerchantPaymentKey;
import com.cakequake.cakequakeback.payment.repo.MerchantPaymentRepo;
import com.cakequake.cakequakeback.payment.sercurity.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TossPayServiceImpl implements TossPayService {
    private final AppConfig appConfig;
    private final MerchantPaymentRepo merchantPaymentRepo;
    private final EncryptionService encryptionService;

    //토스페이 기본 URL
    @Value("${spring.pg.toss.base-url}")
    private String tossBaseUrl;

    //우리 서비스 기본 URL
    @Value("${spring.app.base-url}")
    private String appBaseUrl;


    private static final String READY_PATH   = "/v1/payments/ready";
    private static final String CANCEL_PATH  = "/v1/payments/{paymentKey}/cancel";
    private static final String REFUND_PATH  = "/v1/payments/{paymentKey}/refund";

    @Override
    public TossPayReadyResponseDTO requestPayment(Long shopId, Long orderId, String customerKey, Long amount) {
        Optional<MerchantPaymentKey> maybeKey = merchantPaymentRepo.findByShopIdAndProviderAndIsActive(shopId,"TOSS",true);
        if(maybeKey.isEmpty()){
            throw new IllegalArgumentException("해당 매장의 토스페이 키를 찾을 수 없습니다.");
        }

        MerchantPaymentKey key = maybeKey.get();
        String secretKey = encryptionService.decrypt(key.getEncryptedApiKey());  //토스 secret key

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secretKey,""); //토스페이는 SecretKey만 Basic Auth로 전달(비어있는 패스워드 사용)

        //Body 구성
        TossPayReadyRequestDTO body = TossPayReadyRequestDTO.builder()
                .amount(amount)
                .orderId(orderId.toString())
                .orderName("CakeOrder#" + orderId)
                .customerKey(customerKey)
                .successUrl(appBaseUrl + "/api/payments/toss/success")
                .failUrl(appBaseUrl + "/api/payments/toss/fail")
                .build();

        HttpEntity<TossPayReadyRequestDTO> request = new HttpEntity<>(body, headers);

        ResponseEntity<TossPayReadyResponseDTO> responseEntity = appConfig.restTemplate().exchange(
          tossBaseUrl + READY_PATH,
          HttpMethod.POST,
          request,
          TossPayReadyResponseDTO.class
        );

        if( !responseEntity.getStatusCode().is2xxSuccessful() || responseEntity.getBody() == null){
            throw new RuntimeException("토스페이 결제 준비 요청 실패");
        }
        return responseEntity.getBody();
    }

    // 실제로 토스페이는 Webhook이나 Redirect 콜백으로 먼저 승인 정보를 보내줍니다.
    // 여기서는 “승인된 paymentKey”만 받아서 객체로 맵핑하는 예시.
    // (실무에선 Webhook 컨트롤러에서 @RequestBody TossPayApproveResponse를 바로 받아도 무방)
//    @Override
//    public TossPayApproveResponseDTO approve(String paymentKey) {
//        Optional<MerchantPaymentKey> maybeKey = merchantPaymentRepo.findByShopIdAndProviderAndIsActive(/*shopId*/0L,"TOSS",true);
//        if(maybeKey.isEmpty()){
//            throw new IllegalArgumentException("토스페이 키를 찾을 수 없습니다");
//        }
//        MerchantPaymentKey key = maybeKey.get();
//        String secretKey = encryptionService.decrypt(key.getEncryptedApiKey());
//
//        //승인 단계 정보를 다시 PG사에 조회하는 경우 -> 웹 훅으로 죄히할 겨웅 필요 없음
//        String approveUrl = tossBaseUrl + "/v1/payments/" + paymentKey;
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_JSON);
//        headers.setBasicAuth(secretKey,"");
//
//        ResponseEntity<TossPayApproveResponseDTO> response = appConfig.restTemplate().exchange(
//                approveUrl,
//                HttpMethod.GET,
//                new HttpEntity<>(headers),
//                TossPayApproveResponseDTO.class
//        );
//        if( !response.getStatusCode().is2xxSuccessful() || response.getBody() == null){
//            throw new RuntimeException("토스페익 결제 승인 정보 조회 실패");
//        }
//        return response.getBody();
//    }


    @Override
    public TossPayCancelResponseDTO cancel(Long shopId, TossPayCancelRequestDTO cancelRequest) {
        Optional<MerchantPaymentKey> maybeKey = merchantPaymentRepo.findByShopIdAndProviderAndIsActive(/*shopId*/0L,"TOSS",true);
        if(maybeKey.isEmpty()){
            throw new IllegalArgumentException("토스페이 키를 찾을 수 없습니다");
        }
        MerchantPaymentKey key = maybeKey.get();
        String secretKey = encryptionService.decrypt(key.getEncryptedApiKey());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secretKey, "");

        HttpEntity<TossPayCancelRequestDTO> request = new HttpEntity<>(cancelRequest, headers);

        Map<String, String> uriVars = new HashMap<>();
        uriVars.put("paymentkey",cancelRequest.getPaymentKey());

        ResponseEntity<TossPayCancelResponseDTO> response = appConfig.restTemplate().exchange(
                tossBaseUrl + CANCEL_PATH,
                HttpMethod.POST,
                request,
                TossPayCancelResponseDTO.class,
                uriVars
        );

        if( !response.getStatusCode().is2xxSuccessful() || response.getBody() == null){
            throw new RuntimeException("토스페이 결제 취소(환불) 요청 실패");
        }


        return response.getBody();
    }

    @Override
    public TossPayRefundResponseDTO refund(Long shopId, TossPayRefundRequestDTO refundRequest) {
        Optional<MerchantPaymentKey> maybeKey =
                merchantPaymentRepo.findByShopIdAndProviderAndIsActive(/*shopId*/0L,"TOSS",true);
        if(maybeKey.isEmpty()){
            throw new IllegalArgumentException("토스페이 키를 찾을 수 없습니다");
        }
        MerchantPaymentKey key = maybeKey.get();
        String secretKey = encryptionService.decrypt(key.getEncryptedApiKey());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBasicAuth(secretKey, "");

        HttpEntity<TossPayRefundRequestDTO> requestEntity = new HttpEntity<>(refundRequest, headers);

        Map<String, String> uriVars = new HashMap<>();
        uriVars.put("paymentKey", refundRequest.getPaymentKey());

        ResponseEntity<TossPayRefundResponseDTO> response = appConfig.restTemplate().exchange(
                tossBaseUrl + REFUND_PATH,
                HttpMethod.POST,
                requestEntity,
                TossPayRefundResponseDTO.class,
                uriVars
        );

        if(!response.getStatusCode().is2xxSuccessful() || response.getBody() == null){
            throw new RuntimeException("토스페이 환불 요청 실패");
        }

        return response.getBody();
    }
}
