package com.cakequake.cakequakeback.cart.repo;

import com.cakequake.cakequakeback.cart.entities.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CartRepo extends JpaRepository<Cart, Long> {

    // 특정 유저의 장바구니 아이템만 조회하기 위한 메서드
    List<Cart> findByMemberUid(long uid);
}
