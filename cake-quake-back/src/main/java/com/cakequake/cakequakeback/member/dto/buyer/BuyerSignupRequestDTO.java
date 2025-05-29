package com.cakequake.cakequakeback.member.dto.buyer;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerSignupRequestDTO {

    @NotBlank(message = "아이디는 필수 입력값입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문/숫자 4~20자로 입력해야 합니다")
    private String userId;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Pattern(regexp = "^(?=.*[가-힣a-zA-Z])([가-힣a-zA-Z0-9]{1,19})$", message = "이름은 한글 또는 영어가 최소 1개 이상 포함되고, 한글, 영어, 숫자 조합으로 20자 미만으로 입력해야합니다.")
    private String uname;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-])[A-Za-z\\d!@#$%^&*()_+=-]{8,20}$",
            message = "비밀 번호의 길이는 최소 8자 이상 최대 20자 이하이고 문자, 숫자, 특수문자가 적어도 하나 이상 포함되어야 합니다."
    )
    private String password;

    @NotBlank(message = "전화번호는 필수 입력값입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호 양식은 XXX-XXXX-XXXX로 입력해야합니다.")
    private String phoneNumber;

    @NotNull(message = "정보 공개 여부는 필수 입력값입니다.")
    private Boolean publicInfo;

    @NotNull(message = "알람 설정 값은 필수입니다.")
    private Boolean alarm;

    @Pattern(regexp = "^(basic)$", message = "가입 유형은 basic만 허용됩니다.")
    private String joinType;

    @Override
    public String toString() {
        return String.format("BuyerSignupRequestDTO{userId='%s', uname='%s', phoneNumber='%s', publicInfo='%s', alarm=%s, joinType='%s'}",
                userId, uname, phoneNumber, publicInfo, alarm, joinType);
    }

}
