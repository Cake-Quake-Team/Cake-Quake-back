package com.cakequake.cakequakeback.cart.repo;

import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.cart.entities.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // CartItem 엔티티의 'cartId' 필드(Cart 타입)를 기준으로 검색
    List<CartItem> findByCart(Cart cart);

    // CartItem 엔티티의 PK('cartItemId')와 'cartId' 필드(Cart 타입)를 기준으로 검색
    Optional<CartItem> findByCartItemIdAndCart_CartId(Long cartItemId, Long cartId);

    // 특정 CartItem ID와 해당 CartItem이 속한 Cart 엔티티를 기준으로 CartItem 삭제
    void deleteByCartItemIdAndCart_CartId(Long cartItemId, Cart cart); //나중에 프론트하면서 적용

    // 특정 Cart의 모든 CartItem 삭제
    void deleteAllByCart_CartId(Cart cart);

    Optional<CartItem> findByCartAndCartItemId(Cart testCart, long cartItemId);
}
