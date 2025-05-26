// src/main/java/com/cakequake/cakequakeback/cart/service/CartService.java
package com.cakequake.cakequakeback.cart.service;

import com.cakequake.cakequakeback.cart.dto.request.AddCartRequestDTO;
import com.cakequake.cakequakeback.cart.dto.request.UpdateCartRequestDTO;
import com.cakequake.cakequakeback.cart.dto.response.CartResponseDTO;
import com.cakequake.cakequakeback.cart.dto.response.CartItemDTO;

public interface CartService {

    // 장바구니 담기
    long addCart(AddCartRequestDTO dto);

    //장바구니 조회
    CartResponseDTO getCartList(long uid);

    //장바구니 항목 변경(수정,추가)
    long updateCart(UpdateCartRequestDTO dto);

    //장바구니 항목 삭제
    void deleteCartItem(long uid, Long cartId);
}
