package com.cakequake.cakequakeback.member.dto.verification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhoneVerificationResponseDTO {

    private boolean success;
    private String message;

}
