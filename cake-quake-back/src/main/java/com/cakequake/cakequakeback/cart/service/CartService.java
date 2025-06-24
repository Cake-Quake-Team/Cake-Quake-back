package com.cakequake.cakequakeback.cart.service;

import com.cakequake.cakequakeback.cart.dto.AddCart;
import com.cakequake.cakequakeback.cart.dto.DeletedCartItem;
import com.cakequake.cakequakeback.cart.dto.GetCart;
import com.cakequake.cakequakeback.cart.dto.UpdateCartItem;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;


@Service
@Transactional
public interface CartService {

    //장바구니에 새 상품을 추가
    AddCart.Response addCart(String userId, AddCart.Request request);

    //특정 회원의 장바구니 전체 조회
    GetCart.Response getCart(String userId);


    //장바구니에 있는 상품 수량 변경
    UpdateCartItem.Response updateCartItem(String userId,UpdateCartItem.Request dto);

    //선택된 장바구니 항목들 삭제
    DeletedCartItem.Response deleteCartItem(String userId,Long cartItemId);

    // ⭐⭐ 모든 장바구니 아이템 삭제 메서드 (새로 추가하거나, 기존 deleteCartItem 오버로드) ⭐⭐
    // CartService 인터페이스에도 이 메서드를 추가해야 합니다.
    void deleteAllCartItems(String userId);
}
