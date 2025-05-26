package com.cakequake.cakequakeback.member.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 소셜 로그인 후 db 등록을 위해 간단한 가입 정보를 받을 때 사용
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SocialSignupRequestDTO {

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    private String userId; // socialId와 동일

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Pattern(regexp = "^[a-zA-Z가-힣]{1,19}$", message = "이름은 한글 또는 영어 1자이상 20자 미만으로 입력해야합니다.")
    private String uname;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password; // 소셜의 경우 비밀번호를 받지 않아서 Random UUID를 해시해서 저장. 로그인에도 사용되지 않음.

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호 양식은 XXX-XXXX-XXXX로 입력해야합니다.")
    private String phoneNumber;

    @NotBlank(message = "정보 공개 여부는 필수 입력값입니다.")
    @Pattern(regexp = "^[yn]{1}$", message = "정보 공개 여부는 y 또는 n만 허용됩니다.")
    private String publicInfo;

    @NotNull(message = "알람 설정 값은 필수입니다.")
    private Boolean alarm;

    @NotBlank(message = "권한은 필수입니다.")
    @Pattern(regexp = "^(BUYER)$", message = "권한은 BUYER만 허용됩니다.")
    private String role;

    @NotBlank
    @Pattern(regexp = "^(kakao|google)$", message = "가입 유형은 kakao 또는 google만 허용됩니다.")
    private String joinType;

}
