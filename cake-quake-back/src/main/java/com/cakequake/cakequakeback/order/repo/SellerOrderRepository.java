package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

    //특정 매장의 특정 날짜의 주문 조회
    @Query("SELECT co.orderId, co.pickupDate,co.pickupTime, co.status, co.shop FROM CakeOrder co " +
            "WHERE co.shop.shopId = :shopId AND " +
            "co.pickupDate = :pickupDate AND " +
            "co.status NOT IN :statusesToExclude")
    List<CakeOrder> findSchedule(
            Long shopId,
            LocalDate pickupDate,
            List<OrderStatus> statuses);

    //특정 날짜/시간에 예약된 주문 조회
    @Query("SELECT co.orderId, co.pickupDate,co.pickupTime, co.status, co.shop FROM CakeOrder co " +
            "WHERE co.shop.shopId = :shopId AND " +
            "co.pickupDate = :pickupDate AND " +
            "co.pickupTime = :pickupTime AND " +
            "co.status NOT IN :statusesToExclude")
    List<CakeOrder> findTimeSchedule(
            Long shopId,
            LocalDate pickupDate,
            LocalTime pickupTime,
            List<OrderStatus> statuses);


}

