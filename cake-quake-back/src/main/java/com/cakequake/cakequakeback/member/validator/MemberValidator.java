package com.cakequake.cakequakeback.member.validator;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.dto.buyer.BuyerSignupRequestDTO;
import com.cakequake.cakequakeback.member.dto.seller.SellerSignupStep1RequestDTO;
import com.cakequake.cakequakeback.member.entities.SocialType;
import com.cakequake.cakequakeback.member.entities.VerificationType;
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

        // 전화번호 중복 검사
        if (memberRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_PHONE); // 702
        }
    }

    public void validateSellerSignup(SellerSignupStep1RequestDTO dto) {

        String userId = dto.getUserId();

        /* 형식 검사 */
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
        // 사업자 등록 번호, 대표자 성명, 개업 일자, 매장명
        if (!isValidBusinessNumber(dto.getBusinessNumber())) {
            throw new BusinessException(ErrorCode.INVALID_BUSINESS_NUMBER); // 607
        }
        if (!isValidBossName(dto.getBossName())) {
            throw new BusinessException(ErrorCode.INVALID_OWNER_NAME); // 609
        }
        if (!isValidOpeningDate(dto.getOpeningDate())) {
            throw new BusinessException(ErrorCode.INVALID_DATETIME_FORMAT); // 642
        }
        if (!isValidShopName(dto.getShopName())) {
            throw new BusinessException(ErrorCode.INVALID_COMPANY_NAME); // 608
        }

        // ID 중복 검사
        if (memberRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_USER_ID); // 701
        }

        // 전화번호 중복 검사
        if (memberRepository.existsByPhoneNumber(dto.getPhoneNumber())) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_PHONE); // 702
        }
    }


    public void validateUserId(String userId) {
        if (!isValidUserId(userId)) {
            throw new BusinessException(ErrorCode.INVALID_USER_ID);
        }
        if (memberRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_USER_ID);
        }
    }

    public void validatePassword(String password) {
        if (!isValidPassword(password)) {
            throw new BusinessException(ErrorCode.INVALID_PASSWORD);
        }
    }

    public void validateName(String name) {
        if (!isValidName(name)) {
            throw new BusinessException(ErrorCode.INVALID_NAME_SHORT);
        }
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (!isValidPhoneNumber(phoneNumber)) {
            throw new BusinessException(ErrorCode.INVALID_PHONE);
        }
        if (memberRepository.existsByPhoneNumber(phoneNumber)) {
            throw new BusinessException(ErrorCode.ALREADY_EXIST_PHONE);
        }
    }

    public void validateJoinType(String joinType) {
        if (!isValidJoinType(joinType)) {
            throw new BusinessException(ErrorCode.INVALID_SIGNUP_TYPE);
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

    private boolean isValidBusinessNumber(String businessNumber) {
        return businessNumber != null && businessNumber.matches("^\\d{3}-\\d{2}-\\d{5}$");
    }

    private boolean isValidBossName(String bossName) {
        return bossName != null && bossName.matches("^[가-힣a-zA-Z]+$");
    }

    private boolean isValidOpeningDate(String openingDate) {
        return openingDate != null && openingDate.matches("^\\d{8}$");
    }

    private boolean isValidShopName(String shopName) {
        return shopName != null && shopName.length() >= 1 && shopName.length() <= 50;
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
