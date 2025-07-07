package com.cakequake.cakequakeback.like.service;

import com.cakequake.cakequakeback.like.dto.cake.GetLikes;
import com.cakequake.cakequakeback.like.dto.cake.ToggleLike;
import jakarta.transaction.Transactional;

// Spring의 @Transactional을 인터페이스에 적용할 수도 있지만,
// 일반적으로 구현체에 적용하는 것이 더 유연합니다.
public interface LikeService {

    /* 특정 케이크 상품에 대한 찜 상태를 토글(추가 또는 취소) */
    ToggleLike.Response toggleLike(String userId, ToggleLike.Request request);

    /* 특정 회원이 찜한 모든 케이크 상품 목록을 조회 */
    GetLikes.Response getLikedItems(String userId);

    /* 특정 케이크 상품의 찜 여부를 확인 */
    boolean isCakeItemLiked(String userId, Long cakeItemId);
}