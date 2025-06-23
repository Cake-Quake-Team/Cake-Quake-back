package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.shop.entities.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 특정 매장의 특정 날짜의 주문 조회
    @Query("SELECT co FROM CakeOrder co " + // 필요한 필드만 가져오는 대신 엔티티 전체를 가져오는 것이 일반적입니다.
            // 물론 특정 필드만 DTO로 가져올 수도 있지만, 현재 CakeOrder가 필요하므로 변경했습니다.
            "WHERE co.shop.shopId = :shopId AND " +
            "co.pickupDate = :pickupDate AND " +
            "co.status NOT IN :statusesToExclude") // orderStatus로 변경 (status가 아닌 실제 필드명)
    List<CakeOrder> findSchedule(
            @Param("shopId") Long shopId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("statusesToExclude") List<OrderStatus> statuses); // <-- 여기를 수정!

    // 특정 날짜/시간에 예약된 주문 조회
    @Query("SELECT co FROM CakeOrder co " + // 마찬가지로 엔티티 전체를 가져오도록 변경
            "WHERE co.shop.shopId = :shopId AND " +
            "co.pickupDate = :pickupDate AND " +
            "co.pickupTime = :pickupTime AND " +
            "co.status NOT IN :statusesToExclude") // orderStatus로 변경
    List<CakeOrder> findTimeSchedule(
            @Param("shopId") Long shopId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("pickupTime") LocalTime pickupTime,
            @Param("statusesToExclude") List<OrderStatus> statuses); // <-- 여기를 수정!

    @Query("SELECT COUNT(co) FROM CakeOrder co " +
            "WHERE co.shop.shopId = :shopId AND " +
            "co.pickupDate = :pickupDate AND " +
            "co.pickupTime = :pickupTime AND " +
            "co.status NOT IN :excludedStatuses")
    long countActiveOrdersForPickupTime(
            @Param("shopId") Long shopId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("pickupTime") LocalTime pickupTime,
            @Param("excludedStatuses") List<OrderStatus> excludedStatuses);

    // ⭐️ [새로 추가] 특정 날짜에 유효한 주문이 있는 매장 목록 조회 (Shop 엔티티를 직접 반환)
    @Query("SELECT DISTINCT co.shop FROM CakeOrder co " +
            "WHERE co.pickupDate = :pickupDate AND " +
            "co.status NOT IN :excludedStatuses")
    List<Shop> findDistinctShopsWithActiveOrdersOnDate(
            @Param("pickupDate") LocalDate pickupDate,
            @Param("excludedStatuses") List<OrderStatus> excludedStatuses);
}
