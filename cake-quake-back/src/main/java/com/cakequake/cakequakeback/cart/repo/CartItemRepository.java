// src/main/java/com/cakequake/cakequakeback/cart/repo/CartItemRepository.java
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

    // Cart에 속한 모든 CartItem 조회 (CakeItem 및 Shop까지 JOIN FETCH)
    @EntityGraph(attributePaths = {"cakeItem", "cakeItem.shop"})
    // regDate (등록일) 기준 오름차순 정렬을 추가하여 일관된 순서를 보장합니다.
    // 다른 기준으로 정렬하고 싶다면 regDate 대신 cartItemId 또는 다른 적절한 필드를 사용할 수 있습니다.
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.cakeItem ciItem JOIN FETCH ciItem.shop WHERE ci.cart = :cart ORDER BY ci.regDate ASC")
    List<CartItem> findByCart(@Param("cart") Cart cart);


    // ⭐ [수정] findByCartAndCartItemIdWithCakeItem: 해당 아이템 조회 시 정렬은 필요 없지만 쿼리 통일성 유지 ⭐
    @Query("SELECT ci FROM CartItem ci JOIN FETCH ci.cakeItem ciItem JOIN FETCH ciItem.shop WHERE ci.cart = :cart AND ci.cartItemId = :cartItemId ORDER BY ci.regDate ASC")
    Optional<CartItem> findByCartAndCartItemIdWithCakeItem(@Param("cart") Cart cart, @Param("cartItemId") Long cartItemId);

    // CartItem 엔티티의 PK('cartItemId')와 'cartId' 필드(Cart 타입)를 기준으로 검색
    // 이 메서드는 이제 findByCartAndCartItemIdWithCakeItem으로 대체되어 사용되지 않을 수 있습니다.
    Optional<CartItem> findByCartItemIdAndCart_CartId(Long cartItemId, Long cartId);

    // 특정 CartItem ID와 해당 CartItem이 속한 Cart 엔티티를 기준으로 CartItem 삭제
    void deleteByCartItemIdAndCart_CartId(Long cartItemId, Cart cart);

    // 특정 Cart의 모든 CartItem 삭제
    void deleteAllByCart_CartId(Long cartId);


}