package com.cakequake.cakequakeback.member.dto.seller;

import com.cakequake.cakequakeback.member.entities.MemberRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 판매자 프로필 조회용 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponseDTO {

    private Long uid;
    private String userId;
    private String name;
    private String phoneNumber;
    private String publicInfo;
    private MemberRole role;
    private LocalDateTime createAt;

//    private ShopSummaryDto shop;  // 매장 요약 정보

}
