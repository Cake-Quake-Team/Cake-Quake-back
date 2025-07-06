package com.cakequake.cakequakeback.like.service;

import com.cakequake.cakequakeback.like.dto.cake.GetLikes;
import com.cakequake.cakequakeback.like.dto.cake.ToggleLike;
import jakarta.transaction.Transactional;

// Spring의 @Transactional을 인터페이스에 적용할 수도 있지만,
// 일반적으로 구현체에 적용하는 것이 더 유연합니다.
public interface LikeService {

    /**
     * 특정 케이크 상품에 대한 찜 상태를 토글(추가 또는 취소)합니다.
     *
     * @param userId 로그인한 회원의 ID
     * @param request 찜할 케이크 상품 ID를 포함하는 요청 DTO
     * @return 찜 작업의 결과 및 최종 찜 상태를 포함하는 응답 DTO
     */
    ToggleLike.Response toggleLike(String userId, ToggleLike.Request request);

    /**
     * 특정 회원이 찜한 모든 케이크 상품 목록을 조회합니다.
     *
     * @param userId 로그인한 회원의 ID
     * @return 찜한 케이크 상품 목록을 포함하는 응답 DTO
     */
    GetLikes.Response getLikedItems(String userId);

    /**
     * 특정 케이크 상품의 찜 여부를 확인합니다.
     *
     * @param userId 로그인한 회원의 ID
     * @param cakeItemId 확인할 케이크 상품의 ID
     * @return 찜 여부 (true: 찜됨, false: 찜 안됨)
     */
    boolean isCakeItemLiked(String userId, Long cakeItemId);
}