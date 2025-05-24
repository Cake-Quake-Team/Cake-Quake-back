package com.cakequake.cakequakeback.member.dto.buyer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerDetailResponseDTO {

    private Long uid; // MemberDetail의 PK이자 Member를 참조하는 ID
    private String badges;
    private LocalDateTime delDate;

    private LocalDateTime regDate;
    private LocalDateTime modDate;

}
