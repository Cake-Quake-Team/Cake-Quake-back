package com.cakequake.cakequakeback.member.dto.auth2;

import lombok.*;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class KakaoUserDTO {

    private String email;
    private String nickname;

    public void setEmail(String email) {
        this.email = email;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
}
