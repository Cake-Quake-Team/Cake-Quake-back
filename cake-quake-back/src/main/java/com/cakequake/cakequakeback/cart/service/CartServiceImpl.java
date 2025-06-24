package com.cakequake.cakequakeback.cart.service;

import com.cakequake.cakequakeback.cart.dto.AddCart;
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

    private Cart getOrCreateCart(Member member) {
        return cartRepository.findByMember(member)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .member(member)
                            .cartTotalPrice(0)
                            .build();
                    return cartRepository.save(newCart);
                });
    }

    private void recalculateCartTotalPrice(Cart cart) {
        if (cart == null) {
            return;
        }
        List<CartItem> itemsInCart = cartItemRepository.findByCartWithCakeItem(cart);
        int cartTotalPrice = itemsInCart.stream()
                .mapToInt(item -> item.getItemTotalPrice() != null ? item.getItemTotalPrice().intValue() : 0)
                .sum();
        cartRepository.save(cart);
    }

    @Override
    public AddCart.Response addCart(String userId, AddCart.Request request) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        Cart cart = getOrCreateCart(member);

        CakeItem cakeItem = cakeItemRepository.findById(request.getCakeItemId())
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSING_CAKE_ITEM_ID));

        Optional<CartItem> existingCartItemOpt = cartItemRepository.findByCartWithCakeItem(cart).stream()
                .filter(ci -> ci.getCakeItem().getCakeId().equals(request.getCakeItemId()))
                .findFirst();

        CartItem savedCartItem;
        int quantity = request.getProductCnt();

        if (quantity < 1 || quantity > 99) {
            throw new BusinessException(ErrorCode.QUANTITY_LIMIT_EXCEEDED, "장바구니 상품 수량은 1개 이상 99개 이하여야 합니다.");
        }

        if (existingCartItemOpt.isPresent()) {
            CartItem existingCartItem = existingCartItemOpt.get();
            int newCount = existingCartItem.getProductCnt() + quantity;
            if (newCount > 99) {
                throw new BusinessException(ErrorCode.QUANTITY_LIMIT_EXCEEDED, "상품의 총 수량이 99개를 초과할 수 없습니다.");
            }
            savedCartItem = CartItem.builder()
                    .cartItemId(existingCartItem.getCartItemId())
                    .cart(existingCartItem.getCart())
                    .cakeItem(existingCartItem.getCakeItem())
                    .productCnt(newCount)
                    .itemTotalPrice((long) cakeItem.getPrice() * newCount)
                    .build();
        } else {
            savedCartItem = CartItem.builder()
                    .cart(cart)
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
                .cname(savedCartItem.getCakeItem().getCname())
                .productCnt(savedCartItem.getProductCnt())
                .itemTotalPrice(savedCartItem.getItemTotalPrice())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public GetCart.Response getCart(String userId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));
        Cart cart = cartRepository.findByMember(member)
                .orElse(null);

        if (cart == null) {
            return GetCart.Response.builder()
                    .items(List.of())
                    .cartTotalPrice(0L)
                    .build();
        }
        List<CartItem> cartItemEntities = cartItemRepository.findByCart(cart); // ✅ findByCartWithCakeItem 대신 findByCart 사용

        List<GetCart.ItemInfo> cartItemDtos = cartItemEntities.stream()
                .map(entity -> GetCart.ItemInfo.builder()
                        .cartItemId(entity.getCartItemId())
                        .cakeId(entity.getCakeItem().getCakeId())
                        .cname(entity.getCakeItem().getCname())
                        .price(entity.getCakeItem().getPrice())
                        .thumbnailImageUrl(entity.getCakeItem().getThumbnailImageUrl())
                        .productCnt(entity.getProductCnt())
                        .itemTotalPrice(entity.getItemTotalPrice())
                        .shopId(entity.getCakeItem().getShop().getShopId()) // ✅ shopId 필드를 추가합니다!
                        .build())
                .collect(Collectors.toList());

        return GetCart.Response.builder()
                .items(cartItemDtos)
                .cartTotalPrice(cart.getCartTotalPrice() != null ? cart.getCartTotalPrice().longValue() : 0L)
                .build();
    }

    @Override
    public UpdateCartItem.Response updateCartItem(String userId,UpdateCartItem.Request requestDto) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));
        Cart cart = cartRepository.findByMember(member)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CART_ID, "해당 사용자의 장바구니를 찾을 수 없습니다."));


        CartItem existingCartItem = cartItemRepository
                .findByCartAndCartItemId(cart, requestDto.getCartItemId())
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.NOT_FOUND_CART_ID,
                        "ID " + requestDto.getCartItemId() +
                                "에 해당하는 장바구니 아이템을 찾을 수 없거나, 사용자 소유가 아닙니다."
                ));

        int newCnt = requestDto.getProductCnt();
        if (newCnt < 1 || newCnt > 99) {
            throw new BusinessException(ErrorCode.QUANTITY_LIMIT_EXCEEDED, "장바구니 수량은 1~99 사이여야 합니다.");
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
    public DeletedCartItem.Response deleteCartItem(String userId, Long cartItemId) {
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));
        Cart cart = cartRepository.findByMember(member)
                .orElse(null); // 사용자의 장바구니가 없을 수도 있음

        if (cart == null) {
            return DeletedCartItem.Response.builder()
                    .deletedCartItemIds(List.of())
                    .message("삭제할 장바구니가 없습니다.")
                    .build();
        }

        CartItem itemToDelete = cartItemRepository.findByCartAndCartItemId(cart, cartItemId)
                .orElseThrow(() -> new BusinessException(
                        ErrorCode.INVALID_CART_ITEMS,
                        "ID " + cartItemId + "에 해당하는 장바구니 아이템을 찾을 수 없거나, 사용자 소유가 아닙니다."
                ));

        cartItemRepository.delete(itemToDelete); // ⭐ 특정 아이템 삭제 ⭐
        recalculateCartTotalPrice(cart); // 장바구니 총 가격 재계산

        return DeletedCartItem.Response.builder()
                .deletedCartItemIds(List.of(cartItemId)) // 삭제된 ID 목록에 해당 아이템만 포함
                .message(userId + " 사용자의 장바구니에 있던 상품 ID " + cartItemId + "가 삭제되었습니다.")
                .build();
    }

    // ⭐⭐ 모든 장바구니 아이템 삭제 메서드 (새로 추가하거나, 기존 deleteCartItem 오버로드) ⭐⭐
    // CartService 인터페이스에도 이 메서드를 추가해야 합니다.
    @Override
    public void deleteAllCartItems(String userId) { // CartService 인터페이스에 이 메서드 추가 필요
        Member member = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_UID));

        Cart cart = cartRepository.findByMember(member)
                .orElse(null); // 사용자의 장바구니가 없을 수도 있음

        if (cart == null) {
            // 삭제할 장바구니가 없으므로 특별한 처리 없이 리턴 (또는 BusinessException)
            return; // 또는 throw new BusinessException(ErrorCode.NOT_FOUND_CART_ID, "삭제할 장바구니를 찾을 수 없습니다.");
        }

        // ⭐ 핵심: deleteAllByCart_CartId에 Cart 객체 대신 cartId를 전달 ⭐
        cartItemRepository.deleteAllByCart_CartId(cart.getCartId()); // ✅ 이제 Long 타입의 cartId를 넘김
        recalculateCartTotalPrice(cart); // 장바구니 총 가격 재계산 (0이 될 것임)
    }

}
