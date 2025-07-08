package com.cakequake.cakequakeback.cart.controller;

import com.cakequake.cakequakeback.cart.dto.AddCart;
import com.cakequake.cakequakeback.cart.dto.GetCart;
import com.cakequake.cakequakeback.cart.dto.UpdateCartItem;
import com.cakequake.cakequakeback.cart.service.CartService;
import com.cakequake.cakequakeback.security.domain.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @Validated @RequestBody AddCart.Request requestDto
    ) {
        AddCart.Response responseDto = cartService.addCart(userId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /** 현재 사용자의 장바구니 전체 조회 */
// ✅ 추천 방식: userId, uid 명확히 분리
    @GetMapping
    public ResponseEntity<GetCart.Response> getCart(
            @AuthenticationPrincipal(expression = "member.userId") String userId
    ) {

        return ResponseEntity.ok(cartService.getCart(userId));
    }

    /** 장바구니 내 특정 아이템 수량 수정 */
    @PatchMapping()
    public ResponseEntity<UpdateCartItem.Response> updateCartItem(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @RequestBody @Validated UpdateCartItem.Request requestDto
    ) {
        return ResponseEntity.ok(cartService.updateCartItem(userId, requestDto));
    }


    /** 장바구니 특정 아이템 삭제 */
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteCartItem(userId, cartItemId);
        return ResponseEntity.noContent().build();
    }

    /** 현재 사용자의 장바구니 전체 삭제 */
    @DeleteMapping
    public ResponseEntity<Void> deleteAllCartItems(
            @AuthenticationPrincipal(expression = "member.userId") String userId
    ) {
        cartService.deleteAllCartItems(userId);
        return ResponseEntity.noContent().build();
    }
}
