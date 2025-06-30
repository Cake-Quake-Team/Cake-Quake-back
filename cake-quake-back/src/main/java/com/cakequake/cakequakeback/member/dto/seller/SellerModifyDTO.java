package com.cakequake.cakequakeback.member.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerModifyDTO {

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Pattern(regexp = "^(?=.*[가-힣a-zA-Z])([가-힣a-zA-Z0-9]{1,20})$", message = "이름은 한글 또는 영어가 최소 1개 이상 포함되고, 한글, 영어, 숫자 조합으로 20자 이내로 입력해야합니다.")
    private String uname; // 닉네임

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 양식은 XXX-XXXX-XXXX로 입력해야합니다.")
    private String phoneNumber;

    public void setUname(String uname) {
        this.uname = uname;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
