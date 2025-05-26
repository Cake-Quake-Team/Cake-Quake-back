package com.cakequake.cakequakeback.cart.service;

import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cart.repo.CartRepository;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final MemberRepository memberRepo;
    private final CartRepository cartRepository;
    //private final CartItemRepository cartItemRepository;
    //private final CakeItemRepository cakeItemRepository;

    @Override
    public Response addCart(Request request) {
        return null;
    }

    @Override
    public Response getCart(Long userId) {
        return null;
    }

    @Override
    public Request updateCartItem(List<CakeItem> cartItems, Request request) {
        return null;
    }

    @Override
    public Request deletedCartItem(Request request) {
        return null;
    }


}