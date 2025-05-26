package com.cakequake.cakequakeback.cart.controller;

import com.cakequake.cakequakeback.cart.dto.request.AddCartRequestDTO;
import com.cakequake.cakequakeback.cart.dto.request.UpdateCartRequestDTO;
import com.cakequake.cakequakeback.cart.dto.response.CartResponseDTO;
import com.cakequake.cakequakeback.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    //장바구니 담기
    @PostMapping("/{userId}/cart")
    public ResponseEntity<Long> addCart(
            @PathVariable Long userId,
            @Validated @RequestBody AddCartRequestDTO dto) {
        return ResponseEntity.ok(cartService.addCart(dto));
    }

    //장바구니 조회
    @GetMapping("/{userId}/cart")
    public ResponseEntity<CartResponseDTO> getCart(@PathVariable Long userId) {
        CartResponseDTO response = cartService.getCartList(userId);
        return ResponseEntity.ok(response);
    }

    //장바구니에 담아놓은 상품 수정(수량,상품 변경)
    @PatchMapping("/{userId}/cart/{cartId}")
    public ResponseEntity<Long> updateCart(@Validated @RequestBody UpdateCartRequestDTO request) {
        long updatedId = cartService.updateCart(request);
        return ResponseEntity.ok(updatedId);
    }

    //장바구니 담아놓은 상품 삭제
    @RequestMapping("/{userId}/cart/{cartId}")
    public ResponseEntity<Void> deleteCartItem(@PathVariable Long userId,
                                               @PathVariable Long cartId) {
        cartService.deleteCartItem(userId, cartId);
        return ResponseEntity.noContent().build();
    }
}