package com.cakequake.cakequakeback.cart.service;

import com.cakequake.cakequakeback.cart.dto.request.AddCartRequestDTO;
import com.cakequake.cakequakeback.cart.dto.request.UpdateCartRequestDTO;
import com.cakequake.cakequakeback.cart.dto.response.CartItemDTO;
import com.cakequake.cakequakeback.cart.dto.response.CartResponseDTO;
import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.cart.repo.CartRepo;
import com.cakequake.cakequakeback.member.entities.Member;
//import com.cakequake.cakequakeback.member.repo.MemberRepo;
import com.cakequake.cakequakeback.shop.entities.Shop;
//import com.cakequake.cakequakeback.shop.repo.ShopRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Log4j2
public class CartServiceImpl implements CartService {

    private final CartRepo cartRepo;
    //권한이 없어 주석으로 처리
//    private final MemberRepo memberRepo;
//    private final ShopRepo shopRepo;

    @Override
    public long addCart(AddCartRequestDTO dto) {
        //주석 처리한 회원/매장 조회
//        Member member = memberRepo.findById(dto.getUserId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid userId"));
//        Shop shop = shopRepo.findById(dto.getShopId())
//                .orElseThrow(() -> new IllegalArgumentException("Invalid shopId"));

        Cart cart = Cart.builder()
//                .member(member)
//                .shop(shop)
                .productCnt(dto.getProductCnt())
                .build();

        //저장 후, 자동 생성된 PK(cartId) 반환
        return cartRepo.save(cart).getCartId();
    }


    @Override
    public CartResponseDTO getCartList(long userId) {
        List<Cart> carts = cartRepo.findByMemberUid(userId);
        List<CartItemDTO> items = carts.stream()
                .map(c -> CartItemDTO.builder()
                        .cakeId(c.getCakeItem().getCakeId())
                        .productCnt(c.getProductCnt())
                        .build())
                .collect(Collectors.toList());

        return CartResponseDTO.builder()
                .userId(userId)
                .items(items)
                .build();
    }

    //장바구니 아이템 수량 수정하고, cartId를 반환
    @Override
    public long updateCart(UpdateCartRequestDTO dto) {
        Cart cart = cartRepo.findById(dto.getCartId())
                .orElseThrow(() -> new IllegalArgumentException("Invalid cartId"));

        cart.updateQuantity(dto.getProductCnt());
        return cart.getCartId();
    }

    //특정 장바구니 아이템 삭제
    @Override
    public void deleteCartItem(long userId, Long cartId) {
        cartRepo.deleteById(cartId);
    }
}