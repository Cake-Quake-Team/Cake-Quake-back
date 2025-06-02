package com.cakequake.cakequakeback.member.dto.verification;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class BusinessVerificationRequestDTO {

    @NotBlank(message = "사업자등록번호는 필수입니다.")
    @Pattern(regexp = "^\\d{10}$", message = "사업자등록번호는 '-' 없이 10자리 숫자여야 합니다.")
    private String b_no;

    @NotBlank(message = "개업일자는 필수입니다.")
    @Pattern(regexp = "^\\d{8}$", message = "개업일자는 YYYYMMDD 형식이어야 합니다.")
    private String start_dt;

    @NotBlank(message = "대표자 성명은 필수입니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "대표자명은 한글 또는 영문만 가능합니다.")
    private String p_nm;

    // 필수 요청 항목 아닌 것들
    @Builder.Default
    private String p_nm2 = "";  // 대표자 성명2
    @Builder.Default
    private String b_nm = "";   // 상호
    @Builder.Default
    private String corp_no = "";    // 법인 등록 번호
    @Builder.Default
    private String b_sector = "";
    @Builder.Default
    private String b_type = "";
    @Builder.Default
    private String b_adr = "";
}
