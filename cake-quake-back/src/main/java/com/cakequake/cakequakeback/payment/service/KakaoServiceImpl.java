package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.common.config.AppConfig;
import com.cakequake.cakequakeback.payment.dto.KakaoPayApproveResponse;
import com.cakequake.cakequakeback.payment.dto.KakaoPayCancelResponseDTO;
import com.cakequake.cakequakeback.payment.dto.KakaoPayReadyResponseDTO;
import com.cakequake.cakequakeback.payment.entities.MerchantPaymentKey;
import com.cakequake.cakequakeback.payment.repo.MerchantPaymentRepo;
import com.cakequake.cakequakeback.payment.sercurity.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KakaoPayService {

    //RestTemplate를 이용해 카카오페이 REST API를 호출
    private final AppConfig appConfig;
    private final MerchantPaymentRepo merchantPaymentRepo;
    private final EncryptionService encryptionService;


    @Value("${spring.pg.kakao.base-url}")
    private String baseUrl;

    @Value("${spring.app.base-url}")
    private String appBaseUrl;

    //카카오페이 결제 준비 요청
    @Override
    public KakaoPayReadyResponseDTO ready(Long shopId, Long orderId, BigDecimal amount) {

        //MerchantPaymentKey 조회 -> 복호화
        MerchantPaymentKey key = merchantPaymentRepo.findByShopIdAndProviderAndIsActive(shopId, "KAKAO",true)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 카카오페이 키가 없습니다"));

//        String cid = encryptionService.decrypt(key.getEncryptedApiKey());
//        String adminKey = encryptionService.decrypt(key.getEncryptedSecret());

        String adminKey = encryptionService.decrypt(key.getEncryptedApiKey());
        String cid      = encryptionService.decrypt(key.getEncryptedSecret());

        // 2) kakaoBaseUrl이 실제로 주입되었는지 로그로 확인
        System.out.println(">>> [DEBUG] kakaoBaseUrl = " + baseUrl);
        // (콘솔에 ">>> [DEBUG] kakaoBaseUrl = https://kapi.kakao.com" 와 같이 찍혀야 합니다.)

        //HTTP 헤더 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK " + adminKey);


//        //요청 파라미터(form-data)구성
//        Map<String,String> params = new HashMap<>();
//        params.put("cid", cid);   //가맹점 코드
//        params.put("partner_order_id", orderId.toString()); //주문 고유 ID
//        params.put("partner_user_id", shopId.toString());  //사용자(혹은 매장)
//        params.put("item_name",  "CakeOrder#" + orderId);  //상품명
//        params.put("quantity",  "1");   //수량
//        params.put("total_amount", amount.toString()); //결제 금액
//        params.put("tax_free_amount", "0");    //면세 금액
//        params.put("approval_url" , appBaseUrl + "/api/payments/kakao/approve");
//        params.put("cancel_url" , appBaseUrl + "/api/payments/kakao/cancel");
//        params.put("fail_url", appBaseUrl + "/api/payments/kakao/fail");
//
//        //Post 요청 실행
//        HttpEntity<Map<String,String>> requestEntity = new HttpEntity<>(params, headers);

        // 4) ▼ 여기를 Map → MultiValueMap 으로 변경
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);
        params.add("partner_order_id", orderId.toString());
        params.add("partner_user_id", shopId.toString());
        params.add("item_name",     "CakeOrder#" + orderId);
        params.add("quantity",      "1");
        params.add("total_amount",  amount.toString());
        params.add("tax_free_amount", "0");
        params.add("approval_url",   appBaseUrl + "/api/payments/kakao/approve");
        params.add("cancel_url",     appBaseUrl + "/api/payments/kakao/cancel");
        params.add("fail_url",       appBaseUrl + "/api/payments/kakao/fail");

        // HttpEntity에 MultiValueMap을 담기
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(params, headers);


        KakaoPayReadyResponseDTO response = appConfig.restTemplate().postForObject(
                baseUrl + "/v1/payment/ready",
                requestEntity,
                KakaoPayReadyResponseDTO.class
        );

        return response;
    }

    //결제 요청 취소 요청
    @Override
    public KakaoPayCancelResponseDTO cancel(Long shopId,String tid, BigDecimal amount) {
        //MerchantPaymentKey 조회 -> 복호화
        MerchantPaymentKey key = merchantPaymentRepo.findByShopIdAndProviderAndIsActive(shopId, "KAKAO",true)
                .orElseThrow(() -> new IllegalArgumentException("활성화된 카카오페이 키가 없습니다"));

        String cid = encryptionService.decrypt(key.getEncryptedApiKey());
        String adminKey = encryptionService.decrypt(key.getEncryptedSecret());

        //HTTP 헤더 세팅
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK" + adminKey);

        //요청 파라미터(from-data) 구성
        Map<String, String> params = new HashMap<>();
        params.put("cid", cid);
        params.put("tid", tid);
        params.put("cancel_amount", amount.toString());

        HttpEntity<Map<String,String>> requestEntity = new HttpEntity<>(params, headers);
        KakaoPayCancelResponseDTO response = appConfig.restTemplate().postForObject(
                baseUrl + "/v1/payment/cancel",
                requestEntity,
                KakaoPayCancelResponseDTO.class
        );

        if(response == null || !"CANCEL".equalsIgnoreCase(response.getStatus())){
            throw  new IllegalStateException("카카오페이 결제 취소 요청이 실패했습니다.");
        }
        return response;
    }

    //카카오페이 결제 승인 API 호출
    @Override
    public KakaoPayApproveResponse approvePayment(String tid, String partnerOrderId, String partnerUserId, String pgToken) {
        Long shopId = Long.valueOf(partnerUserId); //partenerUserId로 shopId쓰는 경우
        MerchantPaymentKey key =merchantPaymentRepo.findByShopIdAndProviderAndIsActive(shopId,"KAKAO",true)
                .orElseThrow(()->
                        new IllegalArgumentException("활성화된 카카오페이 키가 없습니다."));
        String cid = encryptionService.decrypt(key.getEncryptedApiKey());
        String adminKey = encryptionService.decrypt(key.getEncryptedSecret());

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("Authorization", "KakaoAK"  + adminKey);

        //  요청 파라미터(form-data) 설정
        Map<String, String> params = new HashMap<>();
        params.put("cid", cid);
        params.put("tid", tid);
        params.put("partner_order_id", partnerOrderId);
        params.put("partner_user_id", partnerUserId);
        params.put("pg_token", pgToken);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(params, headers);
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
