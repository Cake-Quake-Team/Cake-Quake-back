package com.cakequake.cakequakeback.cart.service;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cart.dto.AddCart;
import com.cakequake.cakequakeback.cart.dto.DeletedCartItem;
import com.cakequake.cakequakeback.cart.dto.GetCart;
import com.cakequake.cakequakeback.cart.dto.UpdateCartItem;
import jakarta.transaction.Transactional;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@Transactional
public interface CartService {

    //장바구니에 새 상품을 추가
    AddCart.Response addCart(String userId, AddCart.Request request);

    //특정 회원의 장바구니 전체 조회
    GetCart.Response getCart(String userId);

    //장바구니에 있는 상품 수량 변경
    UpdateCartItem.Response updateCartItem(String userId, UpdateCartItem.Request request);

    //선택된 장바구니 항목들 삭제
    DeletedCartItem.Response deleteCartItem(String userId);
}
