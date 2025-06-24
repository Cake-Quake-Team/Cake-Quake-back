package com.cakequake.cakequakeback.cart.repo;

import com.cakequake.cakequakeback.cart.entities.Cart;
import com.cakequake.cakequakeback.cart.entities.CartItem;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // CartItem 엔티티의 'cartId' 필드(Cart 타입)를 기준으로 검색
    //List<CartItem> findByCart(Cart cart);

    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.cakeItem WHERE ci.cart = :cart")
    List<CartItem> findByCartWithCakeItem(@Param("cart") Cart cart);

    // ✅ 새로운 메서드: CartItem 조회 시 CakeItem과 CakeItem의 Shop까지 함께 Fetch Join
    @EntityGraph(attributePaths = {"cakeItem", "cakeItem.shop"}) // "cakeItem.shop" 경로로 Shop 엔티티까지 Fetch
    List<CartItem> findByCart(Cart cart); // Cart 객체로 조회하는 기존 findByCart 메서드 오버라이딩


    // CartItem 엔티티의 PK('cartItemId')와 'cartId' 필드(Cart 타입)를 기준으로 검색
    Optional<CartItem> findByCartItemIdAndCart_CartId(Long cartItemId, Long cartId);

    // 특정 CartItem ID와 해당 CartItem이 속한 Cart 엔티티를 기준으로 CartItem 삭제
    void deleteByCartItemIdAndCart_CartId(Long cartItemId, Cart cart); //나중에 프론트하면서 적용

    // 특정 Cart의 모든 CartItem 삭제
    void deleteAllByCart_CartId(Long cartId);

    Optional<CartItem> findByCartAndCartItemId(Cart testCart, long cartItemId);
}
