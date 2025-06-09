package com.cakequake.cakequakeback.member.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SigninRequestDTO {

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    private String userId;
    
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
