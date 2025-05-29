package com.cakequake.cakequakeback.member.validator;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import com.cakequake.cakequakeback.member.repo.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberValidator {

    private final MemberRepository memberRepository;

    public void validateSignupRequest(BuyerSignupRequestDTO dto) {

        String userId = dto.getUserId();

        // 형식 검사
        if (!isValidUserId(dto.getUserId())) {
            throw new BusinessException(ErrorCode.INVALID_USER_ID); // 601
        }
        if (!isValidPassword(dto.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD); // 602
        }
        if (!isValidName(dto.getUname())) {
            throw new BusinessException(ErrorCode.INVALID_NAME_SHORT); // 603
        }
        if (!isValidPhoneNumber(dto.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.INVALID_PHONE); // 604
        }
        if (!isValidJoinType(dto.getJoinType())) {
            throw new BusinessException(ErrorCode.INVALID_SIGNUP_TYPE); // 606
        }

        // ID 중복 검사
        if (memberRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_USER_ID); // 701
        }
    }

    // 검증용 정규식
    private boolean isValidUserId(String id) {
        return id != null && id.matches("^[a-zA-Z0-9]{4,20}$");
    }

    private boolean isValidPassword(String pw) {
        return pw != null && pw.matches("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$");
    }

    private boolean isValidName(String name) {
        return name != null && name.matches("^(?=.*[가-힣a-zA-Z])([가-힣a-zA-Z0-9]{1,19})$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^\\d{3}-\\d{4}-\\d{4}$");
    }

    // "KAKAO", "GOOGLE", "BASIC" 외 값은 예외 발생
    private boolean isValidJoinType(String joinType) {
        try {
            SocialType.from(joinType); // enum 내부에서 toUpperCase 처리
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

}
