package com.cakequake.cakequakeback.member.dto.verification;

import com.cakequake.cakequakeback.member.entities.VerificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

// 인증 번호 검증 시 사용
@Getter
@Setter
public class PhoneVerificationCheckDTO {

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phoneNumber;

    @NotBlank(message = "인증번호는 필수입니다.")
    private String code;

    @NotNull
    private VerificationType type;

}
