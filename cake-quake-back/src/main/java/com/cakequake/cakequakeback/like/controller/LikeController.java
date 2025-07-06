package com.cakequake.cakequakeback.like.controller; // like 패키지 안에 controller 폴더에 위치

import com.cakequake.cakequakeback.like.dto.cake.GetLikes;
import com.cakequake.cakequakeback.like.dto.cake.ToggleLike;
import com.cakequake.cakequakeback.like.service.LikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/buyer/profile/likes") // 케이크 찜 관련 API 엔드포인트
@Validated
public class LikeController {

    private final LikeService likeService;

    /* 특정 케이크 상품의 찜 상태를 토글(추가/취소) */
    @PostMapping("cake/toggle") // POST 요청으로 토글 (상태 변경)
    public ResponseEntity<ToggleLike.Response> toggleLike(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @Validated @RequestBody ToggleLike.Request request
    ) {
        log.info("찜 토글 요청: 회원 ID={}, 케이크 ID={}", userId, request.getCakeItemId());
        ToggleLike.Response response = likeService.toggleLike(userId, request);
        return ResponseEntity.ok(response); // 200 OK
    }

    /* 특정 회원이 찜한 모든 케이크 상품 목록을 조회 */
    @GetMapping("/cake") // GET 요청으로 목록 조회
    public ResponseEntity<GetLikes.Response> getLikedItems(
            @AuthenticationPrincipal(expression = "member.userId") String userId
    ) {
        log.info("찜한 케이크 상품 목록 조회 요청: 회원 ID={}", userId);
        GetLikes.Response response = likeService.getLikedItems(userId);
        return ResponseEntity.ok(response); // 200 OK
    }

    /* 특정 케이크 상품의 찜 여부를 확인. 예를 들어, 상품 상세 페이지에서 찜 버튼의 초기 상태를 표시할 때 */
    @GetMapping("/cake/status/{cakeItemId}") // GET 요청으로 상태 조회
    public ResponseEntity<Boolean> isCakeItemLiked(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @PathVariable Long cakeItemId
    ) {
        log.info("케이크 ID {} 찜 여부 확인 요청: 회원 ID={}", cakeItemId, userId);
        boolean isLiked = likeService.isCakeItemLiked(userId, cakeItemId);
        return ResponseEntity.ok(isLiked); // 200 OK
    }
}