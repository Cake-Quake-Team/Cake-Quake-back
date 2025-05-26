//가게 상세 정보 등록, 수정 -Request
package com.cakequake.cakequakeback.shop.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Builder

public class ShopDetailRequestDTO {

    private Long userId; // 판매자 ID (혹은 로그인 정보에서 추출)
    private String businessNumber;
    private String shopName;
    private String address;
    private String phone;
    private String content;
    private String operationHours;
    private String closeDays;
    private String websiteUrl;
    private String instagramUrl;

    public ShopDetailRequestDTO(Long userId, String businessNumber, String shopName,
                                String address, String phone, String content, String operationHours,
                                String closeDays, String websiteUrl, String instagramUrl) {
        this.userId = userId;
        this.businessNumber = businessNumber;
        this.shopName = shopName;
        this.address = address;
        this.phone = phone;
        this.content = content;
        this.operationHours = operationHours;
        this.closeDays = closeDays;
        this.websiteUrl = websiteUrl;
        this.instagramUrl = instagramUrl;
    }
}
