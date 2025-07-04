package com.cakequake.cakequakeback.like.dto.cake; // like 패키지 안에 dto 폴더에 위치

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

public class GetLikes {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private List<LikeInfo> likedItems; // 찜한 케이크 상품 정보 목록
        private int totalCount;           // (선택 사항) 총 찜 상품 개수 (페이징용)
        // private int totalPages;             // (선택 사항) 총 페이지 수 (페이징용)
        // private int currentPage;            // (선택 사항) 현재 페이지 (페이징용)
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class LikeInfo {
        private Long shopId;
        private String shopName;
        private Long likeId;         // Like 엔티티의 ID
        private Long cakeItemId;     // 찜된 케이크 상품의 ID
        private String cakeName;     // 케이크 상품명
        private String thumbnailUrl; // 케이크 상품 썸네일 이미지 URL
        private int price;           // 케이크 상품의 가격
    }
}