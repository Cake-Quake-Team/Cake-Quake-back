package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationRequestWrapper;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationResponseDTO;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationResultDTO;
import com.cakequake.cakequakeback.member.dto.verification.BusinessVerificationRequestDTO;
import com.cakequake.cakequakeback.member.service.seller.BusinessVerificationService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "logging.level.com.cakequake.cakequakeback.member=DEBUG",
        "logging.level.root=INFO"
})
public class BusinessVerificationServiceTest {

    @Autowired
    private BusinessVerificationService service;

    @MockitoBean
    private RestTemplate restTemplate;

    @Test
    @DisplayName("사업자 진위여부 확인 테스트")
    public void verify_validBusinessNumber_returnsSuccess() {
        // given
        // 요청에 필수인 값들(실제 정보 넣어야 함)
        BusinessVerificationRequestDTO requestDTO = BusinessVerificationRequestDTO.builder()
                .b_no("1234567890")
                .start_dt("20200601") // 개업 일자
                .p_nm("선우주")
                .build();

        BusinessVerificationRequestWrapper wrapper = new BusinessVerificationRequestWrapper();
        wrapper.setBusinesses(List.of(requestDTO));

        // API 응답을 흉내 낼 가짜 응답
        BusinessVerificationResponseDTO mockResponse = BusinessVerificationResponseDTO.builder()
                .status_code("OK")
                .request_cnt(1)
                .valid_cnt(1)
                .data(List.of(BusinessVerificationResultDTO.builder()
                        .b_no("1234567890")
                        .valid("01")
                        .valid_msg("정상 등록된 사업자입니다.")
                        .build()))
                .build();

        // RestTemplate.exchange()가 호출됐을 때 이 응답을 주게 설정
        ResponseEntity<BusinessVerificationResponseDTO> mockEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(HttpMethod.POST),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(BusinessVerificationResponseDTO.class)
        )).thenReturn(mockEntity);

        // when
        ApiResponseDTO response = service.verify(requestDTO);

        // then
        Assertions.assertTrue(response.isSuccess());
        Assertions.assertEquals("정상 등록된 사업자입니다.", response.getMessage());

    }

    @Test
    @DisplayName("사업자 진위여부 확인 테스트 - 유효하지 않은 사업자")
    public void verify_invalidBusinessNumber_returnsFailure() {
        // given
        BusinessVerificationRequestDTO requestDTO = BusinessVerificationRequestDTO.builder()
                .b_no("1234567890")
                .start_dt("20200601") // 개업 일자
                .p_nm("선우주")
                .build();

        BusinessVerificationResponseDTO mockResponse = BusinessVerificationResponseDTO.builder()
                .status_code("OK")
                .request_cnt(1)
                .valid_cnt(1)
                .data(List.of(BusinessVerificationResultDTO.builder()
                        .b_no("9999999999")
                        .valid("02") // 유효하지 않은 사업자
                        .valid_msg("확인할 수 없습니다.")
                        .build()))
                .build();

        ResponseEntity<BusinessVerificationResponseDTO> mockEntity = new ResponseEntity<>(mockResponse, HttpStatus.OK);

        Mockito.when(restTemplate.exchange(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.eq(HttpMethod.POST),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(BusinessVerificationResponseDTO.class)
        )).thenReturn(mockEntity);

        // when
        ApiResponseDTO response = service.verify(requestDTO);

        // then
        Assertions.assertFalse(response.isSuccess());
        Assertions.assertEquals("확인할 수 없습니다.", response.getMessage());
    }

}
