package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationCheckDTO;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationRequestDTO;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationResponseDTO;
import com.cakequake.cakequakeback.member.entities.PhoneVerification;
import com.cakequake.cakequakeback.member.entities.VerificationType;
import com.cakequake.cakequakeback.member.repo.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class PhoneVerificationServiceImpl implements PhoneVerificationService {

    private final PhoneVerificationRepository repository;

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRES_MINUTES = 3;   // 인증번호 유효시간. 3분
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    // 인증번호 발송
    @Override
    public PhoneVerificationResponseDTO sendVerificationCode(PhoneVerificationRequestDTO requestDTO) {
        String phoneNumber = requestDTO.getPhoneNumber();
        String code = generateRandomCode(CODE_LENGTH);
        VerificationType type = requestDTO.getType();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(EXPIRES_MINUTES); // 만료 시간 계산

        Optional<PhoneVerification> verificationOpt = repository.findByPhoneNumberAndType(phoneNumber, type);

        if (verificationOpt.isPresent()) {
            // 기존 인증 요청이 있을 경우
            PhoneVerification existing = verificationOpt.get();

            // 인증번호 재전송 제한: 최근 요청이 1분 이내일 경우 차단
            if (existing.getModDate() != null &&
                    existing.getModDate().isAfter(now.minusSeconds(RESEND_COOLDOWN_SECONDS))) {

                return PhoneVerificationResponseDTO.builder()
                        .success(false)
                        .message("인증번호는 1분 후에 다시 요청할 수 있습니다.")
                        .build();
            } // end if

            // 기존 요청에 새로운 코드와 만료 시간 업데이트
            existing.changeCode(code, expiresAt);
            repository.save(existing);
        } else {
            // 최초 인증 요청 저장
            PhoneVerification newVerification = PhoneVerification.builder()
                    .phoneNumber(phoneNumber)
                    .type(type)
                    .code(code)
                    .expiresAt(expiresAt)
                    .build();
            repository.save(newVerification);
        }

        // 실제 환경에서는 문자 API 호출해야 함
        log.info("휴대폰 인증번호 전송: " + phoneNumber + " / 코드: " + code);

        return PhoneVerificationResponseDTO.builder()
                .success(true)
                .message("인증번호가 전송되었습니다.")
                .build();
    }

    // 인증번호 검증
    @Override
    public PhoneVerificationResponseDTO verifyCode(PhoneVerificationCheckDTO checkDTO) {
        String phoneNumber = checkDTO.getPhoneNumber();
        String code = checkDTO.getCode();
        VerificationType type = checkDTO.getType();

        // 번호와 코드로 인증 요청 조회
        Optional<PhoneVerification> foundOpt = repository.findByPhoneNumberAndCodeAndType(phoneNumber, code, type);

        // 일치하는 인증 정보 없으면 실패 응답
        if (foundOpt.isEmpty()) {
            return PhoneVerificationResponseDTO.builder()
                    .success(false)
                    .message("인증번호가 일치하지 않습니다.")
                    .build();
        }

        PhoneVerification verification = foundOpt.get();

        // 이미 인증된 번호면 실패 응답
        if (verification.isVerified()) {
            return PhoneVerificationResponseDTO.builder()
                    .success(false)
                    .message("이미 인증이 완료된 번호입니다.")
                    .build();
        }

        // 인증번호가 만료되었으면 실패 응답
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            return PhoneVerificationResponseDTO.builder()
                    .success(false)
                    .message("인증번호가 만료되었습니다.")
                    .build();
        }

        // 검증 성공 → 인증 완료 처리
        verification.changeVerified(true);
        repository.save(verification);

        return PhoneVerificationResponseDTO.builder()
                .success(true)
                .message("인증이 완료되었습니다.")
                .build();
    }

    // 랜덤 숫자 문자열 생성 6자리
    private String generateRandomCode(int length) {
        Random random = new Random();
        return String.format("%0" + length + "d", random.nextInt((int) Math.pow(10, length)));
    }
}
