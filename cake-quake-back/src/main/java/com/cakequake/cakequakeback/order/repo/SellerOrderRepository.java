package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SellerOrderRepository extends JpaRepository<CakeOrder, Long> {

    //특정 shop의 주문 목록 페이징 조회
    @Query(
            value = "SELECT DISTINCT oi.cakeOrder " +
                    "FROM CakeOrderItem oi " +
                    "JOIN oi.cakeItem ci " +
                    "WHERE ci.shop = :shopId",
            countQuery = "SELECT COUNT(DISTINCT oi2.cakeOrder) " +
                    "FROM CakeOrderItem oi2 " +
                    "JOIN oi2.cakeItem ci2 " +
                    "WHERE ci2.shop= :shopId"
    )
    Page<CakeOrder> findByShopId(Long shopId, Pageable pageable);

    //특정 shop 주문 조회
    @Query(
            "SELECT oi.cakeOrder " +
                    "FROM CakeOrderItem oi " +
                    "JOIN oi.cakeItem ci " +
                    "WHERE ci.shop = :shopId " +
                    "  AND oi.cakeOrder.orderId = :orderId"
    )
    Optional<CakeOrder> findByOrderIdAndShopId(Long orderId, Long shopId);
}

