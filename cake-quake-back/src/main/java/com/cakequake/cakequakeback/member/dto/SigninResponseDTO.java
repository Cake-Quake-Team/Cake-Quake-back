package com.cakequake.cakequakeback.member.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SigninResponseDTO {

    private String accessToken;
    private String refreshToken;

    private String userId;      // 로그인 ID
    private String uname;       // 사용자 표시 이름(닉네임)
    private String role;        // BUYER, SELLER, ADMIN 등
}
