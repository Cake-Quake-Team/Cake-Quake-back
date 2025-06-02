package com.cakequake.cakequakeback.member.service.seller;

import com.cakequake.cakequakeback.common.config.BusinessApiProperties;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationRequestWrapper;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationResponseDTO;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationResultDTO;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationRequestDTO;
import com.cakequake.cakequakeback.shop.repo.ShopRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;


import java.nio.charset.StandardCharsets;
import java.util.List;


@Service
@Transactional
@Slf4j
public class BusinessVerificationServiceImpl implements BusinessVerificationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final BusinessApiProperties properties;
    private final ObjectMapper objectMapper;
    private final ShopRepository shopRepository;

    public BusinessVerificationServiceImpl(BusinessApiProperties properties, ObjectMapper objectMapper, ShopRepository shopRepository) {
        this.properties = properties;
        this.objectMapper = objectMapper;
        this.shopRepository = shopRepository;
    }

    // 사업자 진위여부 확인
    @Override
    public ApiResponseDTO verify(BusinessVerificationRequestDTO requestDTO) {

        // 이미 등록된 사업자 번호 확인
//        if (shopRepository.existsByBusinessNumber(requestDTO.getB_no())) {
//            throw new BusinessException(ErrorCode.ALREADY_EXIST_BUSINESS_NUM);  // 703
//        }

        log.debug("---BusinessVerificationServiceImpl---verify 호출---");
        log.debug("requestDTO: {}", requestDTO);

        String serviceKey = properties.getServiceKey();
//        log.debug("serviceKey: {}", serviceKey);

        String url = "https://api.odcloud.kr/api/nts-businessman/v1/validate?serviceKey=" + serviceKey;
//        log.debug("url: {}", url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // 바디 부분_api 요청 구조 따라서 businesses 로 감싸서 DTO 전달
        BusinessVerificationRequestWrapper wrapper = new BusinessVerificationRequestWrapper();
        wrapper.setBusinesses(List.of(requestDTO));

        HttpEntity<BusinessVerificationRequestWrapper> entity = new HttpEntity<>(wrapper, headers);

        try {
            ResponseEntity<BusinessVerificationResponseDTO> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    entity,
                    BusinessVerificationResponseDTO.class
            );

            BusinessVerificationResponseDTO responseBody = response.getBody();

            if (responseBody == null || responseBody.getData() == null || responseBody.getData().isEmpty()) {
                throw new BusinessException(ErrorCode.BUSINESS_VERIFICATION_FAILED);
            }

            BusinessVerificationResultDTO result = responseBody.getData().get(0);
            log.debug("result.getValid(): {}, getValid_msg(): {}", result.getValid(), result.getValid_msg());

            String validCode = result.getValid();
            String message = null;

            if ("01".equals(validCode)) {
                message = "정상 등록된 사업자입니다.";
            } else if ("02".equals(validCode)) {
                message = "확인할 수 없습니다.";
            }

            return ApiResponseDTO.builder()
                    .success("01".equals(validCode)) // 응답 받은 valid 값이 01이면 정상
                    .message(message)
                    .build();

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("API 요청 실패. 상태 코드: {}, 응답 바디: {}", e.getStatusCode(), e.getResponseBodyAsString());

            String responseBody = e.getResponseBodyAsString();

            try {
                BusinessVerificationResponseDTO errorResponse = objectMapper.readValue(responseBody, BusinessVerificationResponseDTO.class);
                handleStatusCode(errorResponse.getStatus_code());

            } catch (JsonProcessingException jsonException) {
                log.error("응답 JSON 파싱 실패", jsonException);
                throw new BusinessException(ErrorCode.BUSINESS_UNKNOWN_ERROR);
            }

            throw new BusinessException(ErrorCode.BUSINESS_UNKNOWN_ERROR);
        } // end try~catch

    }

    private void handleStatusCode(String statusCode) {
        switch (statusCode) {
            case "TOO_LARGE_REQUEST":
                throw new BusinessException(ErrorCode.BUSINESS_TOO_LARGE_REQUEST);
            case "BAD_JSON_REQUEST":
                throw new BusinessException(ErrorCode.BUSINESS_BAD_JSON);
            case "REQUEST_DATA_MALFORMED":
                throw new BusinessException(ErrorCode.BUSINESS_MALFORMED_DATA);
            case "INTERNAL_ERROR":
                throw new BusinessException(ErrorCode.BUSINESS_INTERNAL_ERROR);
            case "HTTP_ERROR":
                throw new BusinessException(ErrorCode.BUSINESS_HTTP_ERROR);
            case "NO_DATA":
                throw new BusinessException(ErrorCode.BUSINESS_NO_DATA);
            case "SERVICE_KEY_ERROR":
                throw new BusinessException(ErrorCode.BUSINESS_INVALID_SERVICE_KEY);
        }
    }

}
