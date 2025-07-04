package com.cakequake.cakequakeback.like.service;

import com.cakequake.cakequakeback.like.dto.shop.GetShopLikes;
import com.cakequake.cakequakeback.like.dto.shop.ToggleShopLike;
import jakarta.transaction.Transactional;

public interface ShopLikeService {

    /**
     * 특정 매장에 대한 찜 상태를 토글(추가 또는 취소)합니다.
     *
     * @param userId 로그인한 회원의 ID
     * @param request 찜할 매장 ID를 포함하는 요청 DTO
     * @return 찜 작업의 결과 및 최종 찜 상태를 포함하는 응답 DTO
     */
    ToggleShopLike.Response toggleShopLike(String userId, ToggleShopLike.Request request);

    /**
     * 특정 회원이 찜한 모든 매장 목록을 조회합니다.
     *
     * @param userId 로그인한 회원의 ID
     * @return 찜한 매장 목록을 포함하는 응답 DTO
     */
    GetShopLikes.Response getLikedShops(String userId);

    /**
     * 특정 매장의 찜 여부를 확인합니다.
     *
     * @param userId 로그인한 회원의 ID
     * @param shopId 확인할 매장의 ID
     * @return 찜 여부 (true: 찜됨, false: 찜 안됨)
     */
    boolean isShopLiked(String userId, Long shopId);
}