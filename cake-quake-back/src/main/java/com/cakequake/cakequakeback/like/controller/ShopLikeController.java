package com.cakequake.cakequakeback.like.controller; // like 패키지 안에 controller 폴더에 위치

import com.cakequake.cakequakeback.like.dto.shop.GetShopLikes;
import com.cakequake.cakequakeback.like.dto.shop.ToggleShopLike;
import com.cakequake.cakequakeback.like.service.ShopLikeService; // ShopLikeService 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/buyer/profile/likes")
@Validated
public class ShopLikeController {

    private final ShopLikeService shopLikeService;

    /* 특정 매장의 찜 상태를 토글(추가/취소) */
    @PostMapping("/shop/toggle") // POST 요청으로 토글 (상태 변경)
    public ResponseEntity<ToggleShopLike.Response> toggleShopLike(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @Validated @RequestBody ToggleShopLike.Request request
    ) {
        log.info("매장 찜 토글 요청: 회원 ID={}, 매장 ID={}", userId, request.getShopId());
        ToggleShopLike.Response response = shopLikeService.toggleShopLike(userId, request);
        return ResponseEntity.ok(response); // 200 OK
    }

    /* 특정 회원이 찜한 모든 매장 목록을 조회 */
    @GetMapping("/shop") // GET 요청으로 목록 조회
    public ResponseEntity<GetShopLikes.Response> getLikedShops(
            @AuthenticationPrincipal(expression = "member.userId") String userId
    ) {
        log.info("찜한 매장 목록 조회 요청: 회원 ID={}", userId);
        GetShopLikes.Response response = shopLikeService.getLikedShops(userId);
        return ResponseEntity.ok(response); // 200 OK
    }

    /* 특정 매장의 찜 여부를 확인. 예를 들어, 매장 상세 페이지에서 찜 버튼의 초기 상태를 표시할 때 사용 */
    @GetMapping("/shop/status/{shopId}") // GET 요청으로 상태 조회
    public ResponseEntity<Boolean> isShopLiked(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @PathVariable Long shopId
    ) {
        log.info("매장 ID {} 찜 여부 확인 요청: 회원 ID={}", shopId, userId);
        boolean isLiked = shopLikeService.isShopLiked(userId, shopId);
        return ResponseEntity.ok(isLiked); // 200 OK
    }
}