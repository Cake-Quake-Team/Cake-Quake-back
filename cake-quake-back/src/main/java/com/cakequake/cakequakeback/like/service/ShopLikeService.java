package com.cakequake.cakequakeback.like.service;

import com.cakequake.cakequakeback.like.dto.shop.GetShopLikes;
import com.cakequake.cakequakeback.like.dto.shop.ToggleShopLike;
import jakarta.transaction.Transactional;

public interface ShopLikeService {

    /* 특정 매장에 대한 찜 상태를 토글(추가 또는 취소) */
    ToggleShopLike.Response toggleShopLike(String userId, ToggleShopLike.Request request);

    /* 특정 회원이 찜한 모든 매장 목록을 조회 */
    GetShopLikes.Response getLikedShops(String userId);

    /* 특정 매장의 찜 여부를 확인 */
    boolean isShopLiked(String userId, Long shopId);
}