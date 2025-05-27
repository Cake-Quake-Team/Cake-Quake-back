package com.cakequake.cakequakeback.member.repo;

import com.cakequake.cakequakeback.member.entities.PhoneVerification;
import com.cakequake.cakequakeback.member.entities.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findByPhoneNumberAndType(String phoneNumber, VerificationType type); // 해당 번호로 기존 인증 요청이 있는지 조회

    boolean existsByPhoneNumberAndType(String phoneNumber, VerificationType type);    // 이미 인증 중인 번호인지 확인

    // 인증번호 검증 시 사용 (번호 + 코드 일치 여부)
    Optional<PhoneVerification> findByPhoneNumberAndCodeAndType(String phoneNumber, String code, VerificationType type);
}
