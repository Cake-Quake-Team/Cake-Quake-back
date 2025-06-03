// src/main/java/com/cakequake/cakequakeback/cart/controller/CartController.java
package com.cakequake.cakequakeback.cart.controller;

import com.cakequake.cakequakeback.cart.dto.AddCart;
import com.cakequake.cakequakeback.cart.dto.GetCart;
import com.cakequake.cakequakeback.cart.dto.UpdateCartItem;
import com.cakequake.cakequakeback.cart.service.CartService;
// import com.cakequake.cakequakeback.common.response.ApiResponse; // ApiResponse 사용 안 함
import com.cakequake.cakequakeback.security.domain.CustomUserDetails; // CustomUserDetails 임포트
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal; // AuthenticationPrincipal 임포트
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Log4j2
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/cart")
@Validated
public class CartController {

    private final CartService cartService;

    /**장바구니에 새 아이템 추가*/
    @PostMapping
    public ResponseEntity<AddCart.Response> addCart(
             @Validated @RequestBody AddCart.Request requestDto,
             @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getMember().getUserId();
        AddCart.Response responseDto = cartService.addCart(userId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    /** 현재 사용자의 장바구니 전체 조회*/
    @GetMapping
    public ResponseEntity<GetCart.Response> getCart(
             @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getMember().getUserId();
        GetCart.Response responseDto = cartService.getCart(userId);
        return ResponseEntity.ok(responseDto);
    }

    /**장바구니 내 특정 아이템 수량 수정*/
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<Void> updateCartItem(
             @PathVariable("cartItemId") Long cartItemId,
             @Validated @RequestBody UpdateCartItem.Request requestDto,
             @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getMember().getUserId();
        cartService.updateCartItem(userId, requestDto);
        return ResponseEntity.ok().build();
    }

    /**장바구니 특정 아이템 삭제*/
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Void> deleteCartItem(
             @PathVariable("cartItemId") Long cartItemId,
             @AuthenticationPrincipal CustomUserDetails userDetails) {

        String userId = userDetails.getMember().getUserId();
        cartService.deleteCartItem(userId);
        return ResponseEntity.ok().build();
    }
}