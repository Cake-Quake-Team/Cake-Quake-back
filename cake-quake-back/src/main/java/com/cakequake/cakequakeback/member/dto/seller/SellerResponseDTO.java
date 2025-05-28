package com.cakequake.cakequakeback.member.dto.seller;

import com.cakequake.cakequakeback.member.entities.MemberRole;
import com.cakequake.cakequakeback.shop.dto.ShopPreviewDTO;
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
    private String name;
    private String phoneNumber;
    private String publicInfo;
    private MemberRole role;
    private LocalDateTime createAt;

    // 매장 요약 정보 리스트
    private List<ShopPreviewDTO> shopPreviews;

    @Override
    public String toString() {
        return String.format("SellerResponseDTO{uid=%d, role=%s, createAt=%s, shopPreviews(size=%d)}",
                uid, role, createAt,
                shopPreviews != null ? shopPreviews.size() : 0); // 판매자가 지닌 매장 개수
    }

}
