package com.cakequake.cakequakeback.cart.controller;

import com.cakequake.cakequakeback.cart.dto.AddCart;
import com.cakequake.cakequakeback.cart.dto.GetCart;
import com.cakequake.cakequakeback.cart.dto.UpdateCartItem;
import com.cakequake.cakequakeback.cart.service.CartService;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/buyer/cart")
@Validated
public class CartController {

    private final CartService cartService;

    /** 장바구니에 새 아이템 추가 */
    @PostMapping
    public ResponseEntity<AddCart.Response> addCart(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Validated @RequestBody AddCart.Request requestDto
    ) {
        String userId = userDetails.getMember().getUserId();
        AddCart.Response responseDto = cartService.addCart(userId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /** 현재 사용자의 장바구니 전체 조회 */
    @GetMapping
    public ResponseEntity<GetCart.Response> getCart(
            @AuthenticationPrincipal String userId
    ) {
        log.info("principal userId={}", userId);
        return ResponseEntity.ok(cartService.getCart(userId));
        //String userId = userD.getMember().getUserId();
        //GetCart.Response responseDto = cartService.getCart(userId);
        //log.info("principal userId",userId);
        //return ResponseEntity.ok(cartService.getCart(userId));
    }

    /** 장바구니 내 특정 아이템 수량 수정 */
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Validated @RequestBody UpdateCartItem.Request requestDto
    ) {
        String userId = userDetails.getMember().getUserId();
        cartService.updateCartItem(userId, requestDto);
        return ResponseEntity.ok().build();
    }

    /** 장바구니 특정 아이템 삭제 */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long cartItemId   // ← 이 변수, 이제 사용합니다
    ) {
        String userId = userDetails.getMember().getUserId();

        // cartItemId를 함께 넘겨줘야 실제로 해당 아이템을 삭제할 수 있어요
        cartService.deleteCartItem(userId, cartItemId);

        // 삭제는 204 No Content가 더 RESTful
        return ResponseEntity.noContent().build();
    }
}
