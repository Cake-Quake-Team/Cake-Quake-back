package com.cakequake.cakequakeback.payment.service;


import com.cakequake.cakequakeback.common.config.AppConfig;
import com.cakequake.cakequakeback.payment.dto.TossPayApproveResponseDTO;
import com.cakequake.cakequakeback.payment.dto.TossPayCancelRequestDTO;
import com.cakequake.cakequakeback.payment.dto.TossPayCancelResponseDTO;
import com.cakequake.cakequakeback.payment.dto.TossPayReadyResponseDTO;
import com.cakequake.cakequakeback.payment.entities.MerchantPaymentKey;
import com.cakequake.cakequakeback.payment.repo.MerchantPaymentRepo;
import com.cakequake.cakequakeback.payment.sercurity.EncryptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

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

    @Override
    public TossPayReadyResponseDTO requestPayment(Long shopId, Long orderId, String customerKey, Long amount) {
        Optional<MerchantPaymentKey> maybeKey = merchantPaymentRepo.findByShopIdAndProviderAndIsActive(shopId,"TOSS",true);
        if(maybeKey.isEmpty()){
            throw new IllegalArgumentException("해당 매장의 토스페이 키를 찾을 수 없습니다.");
        }

        MerchantPaymentKey key = maybeKey.get();
        String secretKey = encryptionService.decrypt(key.getEncryptedApiKey());  //토스 secret key

        HttpHeaders headers = new HttpHeaders();
        return null;
    }

    @Override
    public TossPayApproveResponseDTO approve(String paymentKey) {
        return null;
    }

    @Override
    public TossPayCancelResponseDTO cancel(Long shopId, TossPayCancelRequestDTO cancelRequest) {
        return null;
    }
}
