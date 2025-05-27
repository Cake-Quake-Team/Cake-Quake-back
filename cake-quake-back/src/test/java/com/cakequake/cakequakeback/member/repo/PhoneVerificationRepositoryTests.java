package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.entities.PhoneVerification;
import com.cakequake.cakequakeback.member.entities.VerificationType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Slf4j
public class PhoneVerificationRepositoryTests {

    @Autowired
    private PhoneVerificationRepository repository;

    // 엔티티 전체 조회 -> 인증번호 변경, 상태 갱신 등에 사용
    @Test
    public void testSaveAndFind() {
        // 테스트용 인증 정보 생성
        PhoneVerification verification = PhoneVerification.builder()
                .phoneNumber("01012345678")
                .type(VerificationType.SIGNUP)
                .code("123456")
                .expiresAt(LocalDateTime.now().plusMinutes(5)) // 만료시간
                .build();

        repository.save(verification);
        log.info("Saved phone verification: {}", verification);

        // 저장된 데이터가 실제로 검색되는지 확인
        Optional<PhoneVerification> found = repository.findByPhoneNumberAndType("01012345678", VerificationType.SIGNUP);

        log.info("Found phone verification: {}", found.orElse(null));

        assertTrue(found.isPresent(), "인증 정보가 존재해야 합니다.");
        assertEquals("123456", found.get().getCode(), "저장된 코드가 일치해야 합니다.");
    }

    // 이미 인증 중인지 존재 여부만 확인 -> 중복 요청 제한, 인증 여부 확인 등에 사용
    @DisplayName("해당 번호로 저장된 데이터가 존재하면 true")
    @Test
    public void testExistsByPhoneNumber() {

        // 인증 요청 정보 저장
        repository.save(PhoneVerification.builder()
                .phoneNumber("01099998888")
                .type(VerificationType.SIGNUP)
                .code("111111")
                .expiresAt(LocalDateTime.now().plusMinutes(5))
                .build());

        boolean exists = repository.existsByPhoneNumberAndType("01099998888", VerificationType.SIGNUP);
        assertTrue(exists);

    }


}
