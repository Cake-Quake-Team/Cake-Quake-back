package com.cakequake.cakequakeback.member.dto.auth2;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoSignupInitDTO {

    private boolean exists; // 카카오 유저의 회원 가입 여부
    private String email;
    private String nickname;
}
