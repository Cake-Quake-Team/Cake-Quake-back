package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationCheckDTO;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationRequestDTO;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationResponseDTO;
import com.cakequake.cakequakeback.member.entities.PhoneVerification;
import com.cakequake.cakequakeback.member.entities.VerificationType;
import com.cakequake.cakequakeback.member.repo.PhoneVerificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@DisplayName("휴대전화 인증 서비스 테스트")
public class PhoneVerificationServiceTests {

    @Autowired
    private PhoneVerificationService service;

    @Autowired
    private PhoneVerificationRepository repository;

    @Test
    @DisplayName("인증번호 요청 성공")
    @Commit
    void testSendVerificationCode_success() {

        PhoneVerificationRequestDTO dto = new PhoneVerificationRequestDTO();
        dto.setPhoneNumber("01012345678");
        dto.setType(VerificationType.SIGNUP);

        PhoneVerificationResponseDTO response = service.sendVerificationCode(dto);

        log.info("응답 결과: {}", response.getMessage());
        assertTrue(response.isSuccess());
        assertEquals("인증번호가 전송되었습니다.", response.getMessage());

        // 실제 저장됐는지 검증
        Optional<PhoneVerification> saved = repository.findByPhoneNumberAndType("01012345678", VerificationType.SIGNUP);

        PhoneVerification phoneVerification = saved.get();

        assertTrue(saved.isPresent());
        assertEquals(6, saved.get().getCode().length());
    }

    @Test
    @DisplayName("인증번호 검증 성공")
    @Commit
    void testVerifyCode_success() {
        // 인증 번호 먼저 저장
        String phone = "01099997777";
        VerificationType type = VerificationType.SIGNUP;
        String code = "123456";

        PhoneVerification saved = PhoneVerification.builder()
                .phoneNumber(phone)
                .type(type)
                .code(code)
                .expiresAt(LocalDateTime.now().plusMinutes(5)) // 만료 안됨
                .verified(false)
                .build();
        repository.save(saved);

        // 검증 요청
        PhoneVerificationCheckDTO checkDTO = new PhoneVerificationCheckDTO();
        checkDTO.setPhoneNumber(phone);
        checkDTO.setCode(code);
        checkDTO.setType(type);

        log.info("phoneNumber: {}, code: {}", checkDTO.getPhoneNumber(), checkDTO.getCode());

        PhoneVerificationResponseDTO response = service.verifyCode(checkDTO);

        assertTrue(response.isSuccess());
        assertEquals("인증이 완료되었습니다.", response.getMessage());

        // DB에 반영됐는지 확인
        PhoneVerification result = repository.findByPhoneNumberAndType(phone, VerificationType.SIGNUP).orElseThrow();
        assertTrue(result.isVerified());
    }

    @Test
    @DisplayName("인증번호 틀림")
    void testVerifyCode_wrongCode() {
        String phone = "01077778888";
        VerificationType type = VerificationType.SIGNUP;

        repository.save(PhoneVerification.builder()
                .phoneNumber(phone)
                .type(type)
                .code("654321")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .verified(false)
                .build());

        PhoneVerificationCheckDTO checkDTO = new PhoneVerificationCheckDTO();
        checkDTO.setPhoneNumber(phone);
        checkDTO.setType(type);
        checkDTO.setCode("000000"); // 틀린 코드


        PhoneVerificationResponseDTO response = service.verifyCode(checkDTO);

        assertFalse(response.isSuccess());
        assertEquals("인증번호가 일치하지 않습니다.", response.getMessage());
    }

    @Test
    @DisplayName("인증번호 만료")
    void testVerifyCode_expired() {
        String phone = "01066667777";

        repository.save(PhoneVerification.builder()
                .phoneNumber(phone)
                .type(VerificationType.SIGNUP)
                .code("111111")
                .expiresAt(LocalDateTime.now().minusMinutes(1)) // 이미 만료
                .verified(false)
                .build());

        PhoneVerificationCheckDTO checkDTO = new PhoneVerificationCheckDTO();
        checkDTO.setPhoneNumber(phone);
        checkDTO.setType(VerificationType.SIGNUP);
        checkDTO.setCode("111111");

        PhoneVerificationResponseDTO response = service.verifyCode(checkDTO);

        assertFalse(response.isSuccess());
        assertEquals("인증번호가 만료되었습니다.", response.getMessage());
    }

}
