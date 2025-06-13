package com.cakequake.cakequakeback.member.service;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.common.utils.PhoneNumberUtils;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationCheckDTO;
import com.cakequake.cakequakeback.member.dto.verification.PhoneVerificationRequestDTO;
import com.cakequake.cakequakeback.member.dto.ApiResponseDTO;
import com.cakequake.cakequakeback.member.entities.PhoneVerification;
import com.cakequake.cakequakeback.member.entities.VerificationType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.repo.PhoneVerificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
@Slf4j
public class PhoneVerificationServiceImpl implements PhoneVerificationService {
    private final MemberRepository memberRepository;

    private final PhoneVerificationRepository repository;

    public PhoneVerificationServiceImpl(PhoneVerificationRepository repository,
                                        MemberRepository memberRepository) {
        this.repository = repository;
        this.memberRepository = memberRepository;
    }

    private static final int CODE_LENGTH = 6;
    private static final int EXPIRES_MINUTES = 3;   // 인증번호 유효시간. 3분
    private static final int RESEND_COOLDOWN_SECONDS = 60;

    // 인증번호 발송
    @Override
    public ApiResponseDTO sendVerificationCode(PhoneVerificationRequestDTO requestDTO) {

        // 전화번호 정규화 (하이픈 제거)
        String rawPhoneNumber = requestDTO.getPhoneNumber();
        String normalized = PhoneNumberUtils.normalize(rawPhoneNumber);

        String code = generateRandomCode(CODE_LENGTH);
        VerificationType type = requestDTO.getType();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(EXPIRES_MINUTES); // 만료 시간 계산

        // 전화번호 형식 검사
        if(!PhoneNumberUtils.isValid(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_PHONE); // 604
        }

        // 전화번호 중복 검사
        if (type == VerificationType.SIGNUP && memberRepository.existsByPhoneNumber(normalized)) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_PHONE); // 702
        }

        Optional<PhoneVerification> verificationOpt = repository.findByPhoneNumberAndType(normalized, type);

        if (verificationOpt.isPresent()) {

            // 기존 인증 요청이 있을 경우
            PhoneVerification existing = verificationOpt.get();

            log.debug("기존 요청 modDate: {}", existing.getModDate());

            // 인증번호 재전송 제한: 최근 요청이 1분 이내일 경우 차단
            if (existing.getModDate() != null &&
                    existing.getModDate().isAfter(now.minusSeconds(RESEND_COOLDOWN_SECONDS))) {

                log.debug("1분 이내 재요청 차단, 메시지 전송 중");
                throw new BusinessException(ErrorCode.TOO_MANY_REQUESTS_IN_SHORT_TIME); // 1102
            } // end if

            // 기존 요청에 새로운 코드와 만료 시간 업데이트
            existing.changeCode(code, expiresAt);
            repository.save(existing);

            log.debug("수정 후 modDate: {}", existing.getModDate());
        } else {
            // 최초 인증 요청 저장
            PhoneVerification newVerification = PhoneVerification.builder()
                    .phoneNumber(normalized)
                    .type(type)
                    .code(code)
                    .expiresAt(expiresAt)
                    .build();
            repository.save(newVerification);
        }

        // 실제 환경에서는 문자 API 호출해야 함
        log.debug("휴대폰 인증번호 전송: {}, / 코드: {} ", normalized, code);

        // 임시로 모달 창으로 보여줄 코드 반환
        return ApiResponseDTO.builder()
                .success(true)
                .message("인증번호가 전송되었습니다.")
                .data(Map.of("verificationCode", code))
                .build();
    }

    // 인증번호 검증
    @Override
    public ApiResponseDTO verifyCode(PhoneVerificationCheckDTO checkDTO) {

        // 전화번호 정규화 (하이픈 제거)
        String rawPhoneNumber = checkDTO.getPhoneNumber();
        String normalized = PhoneNumberUtils.normalize(rawPhoneNumber);

//        String phoneNumber = checkDTO.getPhoneNumber();
        String code = checkDTO.getCode();
        VerificationType type = checkDTO.getType();

        // 전화번호 또는 코드 형식 검증
        if (!PhoneNumberUtils.isValid(normalized)) {
            throw new BusinessException(ErrorCode.INVALID_PHONE); // 604
        }
        if (!isValidOtp(code)) {
            throw new BusinessException(ErrorCode.INVALID_OTP_FORMAT); // 622
        }

        // 번호와 코드로 db에 인증 요청 조회
        PhoneVerification verification = repository.findByPhoneNumberAndCodeAndType(normalized, code, type)
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_OTP)); // 807_잘못된 인증

        // 이미 이 인증 요청은 완료된 상태
        log.debug("현재 verified 상태: {}", verification.isVerified());
        if (verification.isVerified()) {
            log.warn("이미 인증 완료된 번호입니다. 예외 발생!");
            throw new BusinessException(ErrorCode.ALREADY_VERIFIED_PHONE); // 716
        }

        // 만료된 인증번호 입력한 경우
        if (verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.EXPIRED_OTP); // 808
        }

        // 검증 성공 → 인증 완료 처리
        verification.changeVerified(true);
        repository.save(verification);

        return ApiResponseDTO.builder()
                .success(true)
                .message("인증에 성공하였습니다.")
                .build();
    }

    // 랜덤 숫자 문자열 생성 6자리
    private String generateRandomCode(int length) {
        Random random = new Random();
        return String.format("%0" + length + "d", random.nextInt((int) Math.pow(10, length)));
    }

    // 인증 코드 형식 검사
    private boolean isValidOtp(String code) {
        return code != null && code.matches("^\\d{6}$"); // 6자리 숫자
    }
}
