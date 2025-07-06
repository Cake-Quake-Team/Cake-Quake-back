package com.cakequake.cakequakeback.like.dto.cake; // like 패키지 안에 dto 폴더에 위치

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ToggleLike {

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Request {
        @NotNull(message = "찜할 케이크 ID는 필수입니다.")
        private Long cakeItemId; // 찜할/찜 취소할 케이크 상품의 ID
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long likeId; // (선택 사항) 생성되거나 처리된 Like 엔티티의 ID (찜 추가 시 주로 사용)
        private Long cakeItemId; // 처리된 케이크 상품의 ID
        private boolean isLiked; // ⭐ 핵심: 케이크 상품의 최종 찜 상태 (true: 찜됨, false: 찜 취소됨) ⭐
        private String message;  // 작업 성공/실패 메시지
        private int totalLikesCount; // (선택 사항) 해당 케이크 상품에 대한 총 찜 개수 (프론트엔드에서 실시간 업데이트 필요 시)
    }
}