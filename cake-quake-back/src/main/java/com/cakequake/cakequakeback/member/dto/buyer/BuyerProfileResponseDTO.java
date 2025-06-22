package com.cakequake.cakequakeback.member.dto.buyer;

import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 구매자 프로필 조회용 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BuyerProfileResponseDTO {

    private Long uid;
    private String userId;
    private String uname;
    private String phoneNumber;
    private Boolean alarm;

    @JsonIgnore // 클라이언트 응답에서 숨김
    private MemberRole role;

    public boolean isSeller() {
        return MemberRole.SELLER.equals(role);
    }

    @Override
    public String toString() {
        return String.format("BuyerResponseDTO{uid=%d, uname=%s, role=%s, alarm=%s}",
                uid, uname, role, alarm);
    }

}
