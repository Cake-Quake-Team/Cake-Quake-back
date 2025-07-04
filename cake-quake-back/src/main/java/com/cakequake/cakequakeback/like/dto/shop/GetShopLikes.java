package com.cakequake.cakequakeback.like.dto.shop;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class GetShopLikes {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private List<ShopLikeInfo> likedShops; // 찜한 매장 정보 목록
        private int totalElements;           // (선택 사항) 총 찜 매장 개수 (페이징용)
        // private int totalPages;             // (선택 사항) 총 페이지 수 (페이징용)
        // private int currentPage;            // (선택 사항) 현재 페이지 (페이징용)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ShopLikeInfo {
        private Long shopLikeId;     // ShopLike 엔티티의 ID
        private Long shopId;         // 찜된 매장의 ID
        private String shopName;     // 매장 이름
        private String shopAddress;  // 매장 주소 (필요 시)
        private String thumbnailUrl; // 매장 대표 이미지 URL (필요 시)
        // 매장의 영업 시간, 휴무일 등 추가 정보 포함 가능
        private String openTime;
        private String closeTime;
        private String closeDays;
    }
}