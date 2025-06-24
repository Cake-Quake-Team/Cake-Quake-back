package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.shop.entities.Shop; // Shop 엔티티 임포트 필요
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SellerOrderRepository extends JpaRepository<CakeOrder, Long> {

    // 기존 쿼리들 (이전에 수정된 내용 포함)...

    // 특정 shop의 주문 목록 페이징 조회
    @Query(
            value = "SELECT DISTINCT oi.cakeOrder " +
                    "FROM CakeOrderItem oi " +
                    "JOIN oi.cakeItem ci " +
                    "WHERE ci.shop.shopId = :shopId",
            countQuery = "SELECT COUNT(DISTINCT oi2.cakeOrder) " +
                    "FROM CakeOrderItem oi2 " +
                    "JOIN oi2.cakeItem ci2 " +
                    "WHERE ci2.shop.shopId = :shopId"
    )
    Page<CakeOrder> findByShopId(Long shopId, Pageable pageable);

    // 특정 shop 주문 조회
    @Query(
            "SELECT oi.cakeOrder " +
                    "FROM CakeOrderItem oi " +
                    "JOIN oi.cakeItem ci " +
                    "WHERE ci.shop.shopId = :shopId " +
                    "  AND oi.cakeOrder.orderId = :orderId"
    )
    Optional<CakeOrder> findByOrderIdAndShopId(Long orderId, Long shopId);

    // 특정 매장의 특정 날짜의 주문 조회
    @Query("SELECT co FROM CakeOrder co " +
            "WHERE co.shop.shopId = :shopId AND " +
            "co.pickupDate = :pickupDate AND " +
            "co.status NOT IN :statusesToExclude")
    List<CakeOrder> findSchedule(
            @Param("shopId") Long shopId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("statusesToExclude") List<OrderStatus> statusesToExclude);

    // 특정 날짜/시간에 예약된 주문 조회
    @Query("SELECT co FROM CakeOrder co " +
            "WHERE co.shop.shopId = :shopId AND " +
            "co.pickupDate = :pickupDate AND " +
            "co.pickupTime = :pickupTime AND " +
            "co.status NOT IN :statusesToExclude")
    List<CakeOrder> findTimeSchedule(
            @Param("shopId") Long shopId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("pickupTime") LocalTime pickupTime,
            @Param("statusesToExclude") List<OrderStatus> statusesToExclude);


    // --- 새로 추가되는 메서드들 ---

    /**
     * 특정 매장의 특정 픽업 날짜/시간에 유효한(취소되지 않은) 주문의 총 개수를 조회합니다.
     * @param shopId 매장 ID
     * @param pickupDate 픽업 날짜
     * @param pickupTime 픽업 시간
     * @param excludedStatuses 제외할 주문 상태 목록 (예: 취소, 노쇼)
     * @return 해당 시간에 유효한 주문 건수
     */
    @Query("SELECT COUNT(co) FROM CakeOrder co " +
            "WHERE co.shop.shopId = :shopId " +
            "AND co.pickupDate = :pickupDate " +
            "AND co.pickupTime = :pickupTime " +
            "AND co.status NOT IN :excludedStatuses")
    Long countActiveOrdersForPickupTime(
            @Param("shopId") Long shopId,
            @Param("pickupDate") LocalDate pickupDate,
            @Param("pickupTime") LocalTime pickupTime,
            @Param("excludedStatuses") List<OrderStatus> excludedStatuses);


    /**
     * 특정 날짜에 유효한(취소되지 않은) 주문이 있는 모든 고유한 매장(Shop) 목록을 조회합니다.
     * @param pickupDate 픽업 날짜
     * @param excludedStatuses 제외할 주문 상태 목록 (예: 취소, 노쇼)
     * @return 해당 날짜에 활성 주문이 있는 Shop 엔티티 목록
     */
    @Query("SELECT DISTINCT co.shop FROM CakeOrder co " +
            "WHERE co.pickupDate = :pickupDate " +
            "AND co.status NOT IN :excludedStatuses")
    List<Shop> findDistinctShopsWithActiveOrdersOnDate(
            @Param("pickupDate") LocalDate pickupDate,
            @Param("excludedStatuses") List<OrderStatus> excludedStatuses);


    // 기존 SellerStatistics 관련 메서드 (만약 있다면)
    Long countByShopShopIdAndRegDateBetween(Long shopId, LocalDateTime startDate, LocalDateTime endDate);
    Long countByShopShopIdAndRegDateBetweenAndStatus(Long shopId, LocalDateTime startDate, LocalDateTime endDate, OrderStatus status);
    @Query("SELECT COUNT(co) FROM CakeOrder co " +
            "WHERE co.shop.shopId = :shopId " +
            "AND co.regDate BETWEEN :startDate AND :endDate " +
            "AND co.status IN :statuses")
    Long countByShopShopIdAndRegDateBetweenAndStatusIn(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("statuses") List<OrderStatus> statuses);

    @Query("SELECT SUM(co.orderTotalPrice) FROM CakeOrder co " +
            "WHERE co.shop.shopId = :shopId " +
            "AND co.regDate BETWEEN :startDate AND :endDate " +
            "AND co.status = :status")
    Double sumOrderTotalPriceByShopShopIdAndRegDateBetweenAndStatus(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") OrderStatus status);

    @Query("SELECT co.status, COUNT(co) FROM CakeOrder co " +
            "WHERE co.shop.shopId = :shopId " +
            "AND co.regDate BETWEEN :startDate AND :endDate " +
            "GROUP BY co.status")
    List<Object[]> countOrderStatusByShopShopIdAndRegDateBetween(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT ci.cakeId, ci.cname, SUM(coi.quantity), SUM(coi.subTotalPrice), ci.thumbnailImageUrl " +
            "FROM CakeOrderItem coi " +
            "JOIN coi.cakeItem ci " +
            "JOIN coi.cakeOrder co " +
            "WHERE co.shop.shopId = :shopId " +
            "AND co.regDate BETWEEN :startDate AND :endDate " +
            "AND co.status = 'PICKUP_COMPLETED' " +
            "GROUP BY ci.cakeId, ci.cname, ci.thumbnailImageUrl " +
            "ORDER BY SUM(coi.quantity) DESC")
    List<Object[]> findTopSellingProductsByShopIdAndRegDateBetween(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}