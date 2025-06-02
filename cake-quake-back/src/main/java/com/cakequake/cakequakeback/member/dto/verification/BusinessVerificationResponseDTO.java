package com.cakequake.cakequakeback.member.dto.verification;

import lombok.*;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class BusinessVerificationResponseDTO {
    private String status_code;
    private int request_cnt;
    private int valid_cnt;
    private List<BusinessVerificationResultDTO> data;
}
