// src/main/java/com/cakequake/cakequakeback/cart/service/CartServiceImpl.java
package com.cakequake.cakequakeback.cart.service;

import com.cakequake.cakequakeback.cart.dto.AddCart;
// DeleteCartItemsDto는 현재 인터페이스에서 사용되지 않으므로 주석 처리 또는 삭제 가능
// import com.cakequake.cakequakeback.cart.dto.DeleteCartItemsDto;
import com.cakequake.cakequakeback.cart.dto.DeletedCartItem;
import com.cakequake.cakequakeback.cart.dto.GetCart;
import com.cakequake.cakequakeback.cart.dto.UpdateCartItem;
import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.cart.entities.CartItem;
import com.cakequake.cakequakeback.cart.repo.CartItemRepository;
import com.cakequake.cakequakeback.cart.repo.CartRepository;
import com.cakequake.cakequakeback.cake.item.entities.CakeItem;
import com.cakequake.cakequakeback.cake.item.repo.CakeItemRepository;
import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.member.repo.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {

    private final MemberRepository memberRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final CakeItemRepository cakeItemRepository;
    /**주어진 Member에 연결된 Cart가 있는지 조회하고,없으면 새 Cart를 생성하여 반환하는 메서드입니다.*/
    private Cart getOrCreateCart(Member member) {
        return cartRepository.findByMember(member)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .member(member) // Cart 엔티티 필드명이 userId(타입 Member)인 경우
                            .cartTotalPrice(0)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
    /**주어진 Cart의 CartItem을 모두 조회하여 합산한 뒤,Cart의 cartTotalPrice를 재계산하여 저장하는 메서드*/
    private void recalculateCartTotalPrice(Cart cart) {
        // 장바구니가 null일 경우를 대비 (실제로는 cart가 null이면 이 메소드가 호출되기 전에 처리되어야 함)
        if (cart == null) {return;}

        List<CartItem> itemsInCart = cartItemRepository.findByCart(cart);
        int cartTotalPrice = itemsInCart.stream()
                .mapToInt(item -> item.getItemTotalPrice() != null ? item.getItemTotalPrice().intValue() : 0)
                .sum();
        //cart.getCartTotalPrice(cartTotalPrice); // 나중에 테스트코드하면서 해볼거
        cartRepository.save(cart); // 변경된 Cart 객체 저장
    }

    @Override
    public AddCart.Response addCart(String userId, AddCart.Request request) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        Cart cart = getOrCreateCart(member);

        CakeItem cakeItem = cakeItemRepository.findById(request.getCakeItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_PRODUCT_ID));

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByCart(cart).stream()
                .filter(ci -> ci.getCakeItem().getCakeId().equals(request.getCakeItemId()))
                .findFirst();

        CartItem savedCartItem;
        int quantity = request.getProductCnt();

        if (quantity < 1 || quantity > 99) {
            // ErrorCode에 QUANTITY_LIMIT_EXCEEDED (수량 제한 초과)가 있다면 사용, 없으면 INVALID_CART_ITEMS 사용
            throw new BusinessException(ErrorCode.QUANTITY_LIMIT_EXCEEDED, "장바구니 상품 수량은 1개 이상 99개 이하여야 합니다.");
        }

        if (existingCartItemOpt.isPresent()) {
            CartItem existingCartItem = existingCartItemOpt.get();
            int newCount = existingCartItem.getProductCnt() + quantity;
            if (newCount > 99) {
                throw new BusinessException(ErrorCode.QUANTITY_LIMIT_EXCEEDED, "상품의 총 수량이 99개를 초과할 수 없습니다.");
            }
            savedCartItem = CartItem.builder()
                    .cartItemId(existingCartItem.getCartItemId()) // 기존 CartItem ID 유지
                    .cart(existingCartItem.getCart())             // 기존 Cart 참조 유지
                    .cakeItem(existingCartItem.getCakeItem())     // 기존 CakeItem 참조 유지
                    .productCnt(newCount)                         // 새로운 수량
                    .itemTotalPrice((long) cakeItem.getPrice() * newCount) // 새 총액 (CakeItem의 가격 getter가 getPrice()라고 가정)
                    .build();
        } else {
            savedCartItem = CartItem.builder()
                    .cart(cart)             // CartItem 엔티티의 Cart 참조 필드명이 'cart'라고 가정
                    .cakeItem(cakeItem)
                    .productCnt(quantity)
                    .itemTotalPrice((long) cakeItem.getPrice() * quantity)
                    .build();
        }
        savedCartItem = cartItemRepository.save(savedCartItem);
        recalculateCartTotalPrice(cart);

        return AddCart.Response.builder()
                .cartItemId(savedCartItem.getCartItemId())
                .cakeItemId(savedCartItem.getCakeItem().getCakeId())
                .cname(savedCartItem.getCakeItem().getCname()) // CakeItem에 getCname()이 있다고 가정
                .productCnt(savedCartItem.getProductCnt())
                .itemTotalPrice(savedCartItem.getItemTotalPrice())
                //.message("상품이 장바구니에 추가/업데이트되었습니다.")
                .build();
    }


    @Override
    @Transactional(readOnly = true)
    public GetCart.Response getCart(String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));
        Cart cart = cartRepository.findByMember(member)
                .orElse(null); // 장바구니가 없을 수도 있음

        if (cart == null) {
            return GetCart.Response.builder()
                    .Items(List.of()) // 빈 리스트
                    .cartTotalPrice(0L)   // 총액 0
                    .build();
        }
        List<com.cakequake.cakequakeback.cart.entities.CartItem> cartItemEntities = cartItemRepository.findByCart(cart);
        List<GetCart.ItemInfo> cartItemDtos = cartItemEntities.stream() // 여기와
                .map(entity -> GetCart.ItemInfo.builder() // 여기의 클래스 이름을 실제 DTO 내부 클래스 이름으로 일치
                        .cartItemId(entity.getCartItemId())
                        // CakeItem의 PK를 가져오는 getter (getId() 또는 getCakeId()) 와
                        // GetCart.Response.CartItemDto의 필드명 (cakeId)을 일치시켜야 함.
                        .cakeId(entity.getCakeItem().getCakeId()) // CakeItem 엔티티의 PK getter가 getId()라고 가정
                        .cname(entity.getCakeItem().getCname())
                        .thumbnailImageUrl(entity.getCakeItem().getThumbnailImageUrl())
                        .productCnt(entity.getProductCnt())
                        .itemTotalPrice(entity.getItemTotalPrice())
                        .build())
                .collect(Collectors.toList());

        // 4. 최종 응답 DTO 빌드 시 올바른 타입의 리스트 사용
        return GetCart.Response.builder()
                .Items(cartItemDtos)
                .cartTotalPrice(cart.getCartTotalPrice() != null ? cart.getCartTotalPrice().longValue() : 0L)
                .build();
    }

    @Override
    public UpdateCartItem.Response updateCartItem(String userId, UpdateCartItem.Request requestDto) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));
        Cart cart = cartRepository.findByMember(member)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CART_ID, "해당 사용자의 장바구니를 찾을 수 없습니다."));

        CartItem existingCartItem = cartItemRepository.findByCartItemIdAndCart_CartId(requestDto.getCartItemId(), cart.getCartId())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CART_ITEMS, "ID " + requestDto.getCartItemId() + "에 해당하는 장바구니 아이템을 찾을 수 없거나, 사용자 소유가 아닙니다."));

        int newCnt = requestDto.getProductCnt();
        if (newCnt < 1 || newCnt > 99) {
            throw new BusinessException(ErrorCode.INVALID_CART_ITEMS, "장바구니 수량은 1~99 사이여야 합니다.");
        }
        CartItem updatedCartItem = CartItem.builder()
                .cartItemId(existingCartItem.getCartItemId())
                .cart(existingCartItem.getCart())
                .cakeItem(existingCartItem.getCakeItem())
                .productCnt(newCnt)
                .itemTotalPrice((long) existingCartItem.getCakeItem().getPrice() * newCnt)
                .build();

        updatedCartItem = cartItemRepository.save(updatedCartItem);
        recalculateCartTotalPrice(cart);

        return UpdateCartItem.Response.builder()
                .cartItemId(updatedCartItem.getCartItemId())
                .updatedProductCnt(updatedCartItem.getProductCnt())
                .updatedItemTotalPrice(updatedCartItem.getItemTotalPrice())
                .message("장바구니 상품 ID " + updatedCartItem.getCartItemId() + "의 수량이 " + newCnt + "개로 변경되었습니다.")
                .build();
    }

    @Override
    public DeletedCartItem.Response deleteCartItem(String userId) {

        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));
        Cart cart = cartRepository.findByMember(member)
                .orElse(null); // 장바구니가 없을 수도 있음

        if (cart == null) {
            // 장바구니가 없는 경우, 삭제할 아이템도 없으므로 빈 결과를 반환하거나 예외 처리
            return DeletedCartItem.Response.builder()
                    .deletedCartItemIds(List.of())
                    .message("삭제할 장바구니가 없습니다.")
                    .build();
        }


        List<CartItem> itemsToDelete = cartItemRepository.findByCart(cart);
        List<Long> deletedIds = new ArrayList<>();

        if (itemsToDelete.isEmpty()) {
            return DeletedCartItem.Response.builder()
                    .deletedCartItemIds(List.of())
                    .message("장바구니에 삭제할 상품이 없습니다.")
                    .build();
        }
        for (CartItem item : itemsToDelete) {
            deletedIds.add(item.getCartItemId());
        }
        cartItemRepository.deleteAllByCart_CartId(cart);

        //cart.updateCartTotalPrice(0);
        cartRepository.save(cart);


        return DeletedCartItem.Response.builder()
                .deletedCartItemIds(deletedIds) // 삭제된 (원래 있던) 아이템들의 ID 목록
                .message(userId + " 사용자의 장바구니에 있던 " + deletedIds.size() + "개 상품이 모두 삭제되었습니다.")
                .build();
    }
}