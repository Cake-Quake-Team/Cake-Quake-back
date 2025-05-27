package com.cakequake.cakequakeback.member.dto.verification;

import com.cakequake.cakequakeback.member.entities.VerificationType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class PhoneVerificationRequestDTO {

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phoneNumber;

    @NotNull
    private VerificationType type;

    private LocalDateTime regDate;
    private LocalDateTime modDate;

}
