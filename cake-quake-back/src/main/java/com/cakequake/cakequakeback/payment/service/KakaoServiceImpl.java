package com.cakequake.cakequakeback.payment.service;

import com.cakequake.cakequakeback.common.config.AppConfig;
import com.cakequake.cakequakeback.payment.dto.KakaoPayCancelResponseDTO;
import com.cakequake.cakequakeback.payment.dto.KakaoPayReadyResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class KakaoServiceImpl implements KakaoPayService {

    //RestTemplate를 이용해 카카오페이 REST API를 호출
    private final AppConfig appConfig;

    @Value("$kakao.admin-key")
    private String adminKey;

    @Value("${kakao.cid}")
    private String cid;

    @Value("kakao.base-url}")
    private String baseUrl;

    @Value("{app.base-url}")
    private String appBaseUrl;


    @Override
    public KakaoPayReadyResponseDTO ready(Long shopId, Long orderId, BigDecimal amount) {
        //HTTP헤더 준비
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + adminKey);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("cid", cid);    //가맹점 코드
        params.add("parter_order_id", orderId.toString());  //가맹점 주문 번호
        params.add("parter_user_id",orderId.toString());    //가맹점 회원 ID
        params.add("item_name", "CakeOrder#" + orderId);
        params.add("quantity", "1");
        params.add("total_amount", amount.toString());
        params.add("tax_free_amount", "0");

        return null;
    }

    @Override
    public KakaoPayCancelResponseDTO cancel(Long shopId,String tid, BigDecimal amount) {
        return null;
    }
}
