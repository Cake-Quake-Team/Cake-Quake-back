package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerOrderRepository extends JpaRepository<CakeOrder, Long> {

    // 판매자(Shop) 기준 페이징 주문 목록 조회
    Page<CakeOrder> findByCakeOrderItems_Cart_Shop(Shop shop, Pageable pageable);

    // 판매자 기준 특정 주문 상세 조회 ?? 어차피 판매자는 전체 주문 상세 조회만 하기로 한 거 아니야??
    //Optional<CakeOrder> findByOrderIdAndCakeOrderItems_Cart_Shop(Long orderId, Shop shop);
}

