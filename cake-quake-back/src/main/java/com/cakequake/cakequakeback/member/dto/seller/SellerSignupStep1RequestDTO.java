package com.cakequake.cakequakeback.member.dto.seller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerSignupStep1RequestDTO {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Pattern(regexp = "^[a-zA-Z0-9]{4,20}$", message = "아이디는 영문/숫자 4~20자로 입력해야 합니다.")
    private String userId;

    @NotBlank(message = "이름은 필수 입력값입니다.")
    @Pattern(regexp = "^(?=.*[가-힣a-zA-Z])([가-힣a-zA-Z0-9]{1,19})$", message = "이름은 한글 또는 영어가 최소 1개 이상 포함되고, 한글, 영어, 숫자 조합으로 20자 미만으로 입력해야합니다.")
    private String uname; // 닉네임

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+=-]).{8,20}$",
            message = "비밀 번호의 길이는 최소 8자 이상 최대 20자 이하이고 문자, 숫자, 특수문자가 적어도 하나 이상 포함되어야 합니다."
    )
    private String password;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{3,4}-\\d{4}$", message = "전화번호 양식은 XXX-XXXX-XXXX로 입력해야합니다.")
    private String phoneNumber;

    @NotBlank(message = "사업자 등록 번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{3}-\\d{2}-\\d{5}$", message = "사업자 등록 번호가 유효하지 않습니다.")
    private String businessNumber;

    @NotBlank(message = "대표자명은 필수 입력 항목입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "대표자명은 한글 또는 영문만 가능합니다.")
    private String bossName;

    @NotBlank(message = "개업일자는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{8}$", message = "개업일자는 YYYYMMDD 형식의 8자리 숫자로 입력해야 합니다.")
    private String openingDate;

    @NotBlank(message = "상호명은 필수 입력 항목입니다.")
    @Size(min = 1, max = 50, message = "상호명 1~50자 사이로 입력해야 합니다.")
    private String shopName;

    @NotBlank(message = "정보 공개 여부는 필수 입력 항목입니다.")
    private Boolean publicInfo;

    @Pattern(regexp = "^(basic)$", message = "가입 유형은 basic만 허용됩니다.")
    private String joinType;

    @NotNull(message = "사업자 등록증 파일은 필수입니다.")
    private MultipartFile businessCertificate;

    @Override
    public String toString() {
        return "SellerSignupStep1RequestDTO{" +
                "userId='" + mask(userId) + '\'' +
                ", uname='" + uname + '\'' +
                ", phoneNumber='" + mask(phoneNumber) + '\'' +
                ", bossName='" + bossName + '\'' +
                ", businessNumber='" + mask(businessNumber) +  // 또는
                ", shopName='" + shopName + '\'' +
                ", publicInfo=" + publicInfo +
                ", businessCertificate=" + (businessCertificate != null ? businessCertificate.getOriginalFilename() : "null") +
                '}';
    }

    private String mask(String input) {
        if (input == null || input.length() < 4) return "***";
        return input.substring(0, 2) + "***" + input.substring(input.length() - 2);
    }
}
