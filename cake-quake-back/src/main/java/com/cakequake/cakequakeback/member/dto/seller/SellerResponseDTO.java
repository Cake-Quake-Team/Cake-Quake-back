package com.cakequake.cakequakeback.member.dto.seller;

import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

// 판매자 프로필 조회용 DTO
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerResponseDTO {

    private Long uid;
    private String userId;
    private String uname;
    private String phoneNumber;

    // 매장 요약 정보
    private ShopPreviewDTO shopPreview;

    @JsonIgnore // 클라이언트 응답에서 숨김
    private MemberRole role;

    public boolean isSeller() {
        return MemberRole.SELLER.equals(role);
    }

    public SellerResponseDTO(Long uid, String userId, String uname, String phoneNumber, MemberRole role) {
        this.uid = uid;
        this.userId = userId;
        this.uname = uname;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    @Override
    public String toString() {
        return String.format("SellerResponseDTO{uid=%d, uname=%s, shopPreviewExists=%s, role=%s}",
                uid, uname, shopPreview != null ? "Y" : "N", role);
    }

}
