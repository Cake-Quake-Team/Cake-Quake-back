package com.cakequake.cakequakeback.member.dto.verification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

// 사업자 진위 여부 응답 중 Data 내부 객체 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BusinessVerificationResultDTO {
    private String b_no;    // 사업자 등록번호
    private String valid;   // 정상 확인
    @JsonProperty("valid_msg")
    private String valid_msg;
}
