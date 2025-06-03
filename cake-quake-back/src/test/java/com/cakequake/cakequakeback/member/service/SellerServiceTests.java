package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep1RequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep2RequestDTO;
import com.cakequake.cakequakeback.member.entities.PendingSellerRequest;
import com.cakequake.cakequakeback.member.repo.PendingSellerRequestRepository;
import com.cakequake.cakequakeback.member.service.seller.SellerService;
import com.cakequake.cakequakeback.member.validator.MemberValidator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@Import({BCryptPasswordEncoder.class})
@Slf4j
@TestPropertySource(properties = {
        "logging.level.com.cakequake.cakequakeback.member=DEBUG",
        "logging.level.root=INFO"
})
public class SellerServiceTests {

    @Autowired
    private PendingSellerRequestRepository pendingSellerRequestRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private MemberValidator memberValidator;

    @Autowired
    private SellerService service;


    @Disabled
    @Test
    @DisplayName("판매자 1단계 등록 성공 테스트")
    void testRegisterStepOne_success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "businessCertificate", "사업자증_신도림1.jpg", "image/jpeg", "fake-content".getBytes());

        SellerSignupStep1RequestDTO dto = SellerSignupStep1RequestDTO.builder()
                .userId("seller2")  // 중복 검사 ㅇ
                .uname("신판매2")
                .password("pass1234!")
                .phoneNumber("010-2222-1112")   // 중복 검사 ㅇ
                .businessNumber("1111111111")   // 사업자 번호 중복 조심. 중복 검사는 진위여부 요청시 별도로 이루어짐.
                .bossName("신도림")
                .openingDate("20200505")
                .shopName("신도림 케이크")
                .joinType("BASIC")
                .businessCertificate(mockFile)
                .build();

        ApiResponseDTO response = service.registerStepOne(dto);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).contains("1단계 저장 성공");

        // tempSellerId를 이용해 2단계 진행 예정이라서 서비스에서 id 반환한 걸 가져옴.
        Long tempId = (Long) response.getData();
        log.debug("tempId: {}", tempId);
        Optional<PendingSellerRequest> saved = pendingSellerRequestRepository.findById(tempId);
        assertThat(saved).isPresent();

        PendingSellerRequest request = saved.get();
        assertThat(request.getBusinessCertificateUrl()).contains(".jpg");
        assertThat(passwordEncoder.matches("pass1234!", request.getPassword())).isTrue();
    }

    @Test
    @DisplayName("판매자 2단계 등록 성공 테스트")
    void testRegisterStepTwo_success() throws Exception {
        // 1단계 먼저 등록
        MockMultipartFile businessFile = new MockMultipartFile(
                "businessCertificate", "사업자증.jpg", "image/jpeg", "business-cert".getBytes());

        SellerSignupStep1RequestDTO step1DTO = SellerSignupStep1RequestDTO.builder()
                .userId("seller3")
                .uname("이판매")
                .password("Password1!")
                .phoneNumber("010-3333-3333")
                .businessNumber("2111111111")
                .bossName("이유리")
                .openingDate("20230101")
                .shopName("테스트 매장")
                .joinType("BASIC")
                .businessCertificate(businessFile)
                .build();

        ApiResponseDTO step1Response = service.registerStepOne(step1DTO);
        Long tempSellerId = (Long) step1Response.getData();

        // 2단계 이미지 파일
        MockMultipartFile shopImage = new MockMultipartFile(
                "shopImage", "매장대표이미지.jpg", "image/jpeg", "shop-img".getBytes());

        MockMultipartFile sanitationCert = new MockMultipartFile(
                "sanitationCertificate", "위생.jpg", "image/jpeg", "sanitation-img".getBytes());

        SellerSignupStep2RequestDTO step2DTO = SellerSignupStep2RequestDTO.builder()
                .tempSellerId(tempSellerId)
                .shopAddress("서울시 강남구 어디동 123")
                .shopPhoneNumber("02-123-4567")
                .openTime(LocalTime.of(10, 00))
                .closeTime(LocalTime.of(20, 00))
                .mainProductDescription("고급 케이크를 전문으로 판매하는 매장입니다.")
                .shopImage(shopImage)
                .sanitationCertificate(sanitationCert)
                .build();

        ApiResponseDTO step2Response = service.registerStepTwo(step2DTO);

        assertThat(step2Response.isSuccess()).isTrue();
        assertThat(step2Response.getMessage()).contains("승인 요청");

        // 저장된 정보 검증
        PendingSellerRequest saved = pendingSellerRequestRepository.findById(tempSellerId)
                .orElseThrow();
        assertThat(saved.getShopImageUrl()).contains(".jpg");
        assertThat(saved.getSanitationCertificateUrl()).contains(".jpg");
        assertThat(saved.getMainProductDescription()).contains("케이크");
        assertThat(saved.getAddress()).isEqualTo("서울시 강남구 어디동 123");
    }

}
