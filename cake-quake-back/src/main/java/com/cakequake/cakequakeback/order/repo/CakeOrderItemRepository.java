package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CakeOrderItemRepository extends JpaRepository<CakeOrderItem, Long> {

    // 특정 주문의 주문 아이템 조회 (기존 메서드)
    List<CakeOrderItem> findByCakeOrder_OrderId(Long orderId);

    // 기존 findTopSellingProductsByShopId (JPQL 쿼리이므로 엔티티 필드명 사용)
    @Query("SELECT ci.cakeId, ci.cname, SUM(coi.quantity), SUM(coi.subTotalPrice), ci.thumbnailImageUrl " +
            "FROM CakeOrderItem coi " +
            "JOIN coi.cakeItem ci " +       // JPQL: coi.cakeItem (엔티티 필드명)
            "JOIN coi.cakeOrder co " +      // JPQL: coi.cakeOrder (엔티티 필드명)
            "WHERE co.shop.shopId = :shopId " + // JPQL: co.shop.shopId (엔티티 관계 경로)
            "AND co.regDate BETWEEN :startDate AND :endDate " +
            "AND co.status = 'PICKUP_COMPLETED' " +
            "GROUP BY ci.cakeId, ci.cname, ci.thumbnailImageUrl " +
            "ORDER BY SUM(coi.quantity) DESC")
    List<Object[]> findTopSellingProductsByShopId(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    // ✅ 구매자/판매자 주문 상세 조회에서 사용:
    // 특정 주문에 속하는 CakeOrderItem 목록과 CakeItem 정보를 함께 가져옴
    @Query("SELECT coi FROM CakeOrderItem coi JOIN FETCH coi.cakeItem WHERE coi.cakeOrder.orderId = :orderId")
    List<CakeOrderItem> findByCakeOrder_OrderIdWithCakeItem(@Param("orderId") Long orderId);

    // ✅ 구매자/판매자 주문 목록 조회에서 사용:
    // 여러 주문 ID에 속하는 CakeOrderItem 목록과 CakeItem 정보를 함께 가져옴 (배치 조회)
    @Query("SELECT coi FROM CakeOrderItem coi JOIN FETCH coi.cakeItem WHERE coi.cakeOrder.orderId IN :orderIds")
    List<CakeOrderItem> findByCakeOrder_OrderIdInWithCakeItem(@Param("orderIds") List<Long> orderIds);



    // 1. 총 판매량 (아이템 총 수량) 조회
    @Query("SELECT SUM(coi.quantity) FROM CakeOrderItem coi " +
            "JOIN coi.cakeOrder co " +
            "WHERE co.shop.shopId = :shopId " + // JPQL: co.shop.shopId
            "AND co.regDate BETWEEN :startDate AND :endDate " +
            "AND co.status = 'PICKUP_COMPLETED'")
    Long sumTotalQuantityByShopIdAndRegDateBetweenAndStatusCompleted(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 2. 월별 판매 추이 (6개월 차트) 데이터 조회
    @Query("SELECT FUNCTION('TO_CHAR', co.regDate, 'YYYY-MM'), SUM(coi.quantity), SUM(coi.subTotalPrice) " +
            "FROM CakeOrderItem coi " +
            "JOIN coi.cakeOrder co " +
            "WHERE co.shop.shopId = :shopId " + // JPQL: co.shop.shopId
            "AND co.regDate BETWEEN :startDate AND :endDate " +
            "AND co.status = 'PICKUP_COMPLETED' " +
            "GROUP BY FUNCTION('TO_CHAR', co.regDate, 'YYYY-MM') " +
            "ORDER BY FUNCTION('TO_CHAR', co.regDate, 'YYYY-MM')")
    List<Object[]> findMonthlySalesTrendByShopIdAndRegDateBetweenAndStatusCompleted(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 3. 상품별 판매 테이블 데이터 조회
    @Query("SELECT ci.cakeId, ci.cname, SUM(coi.quantity), SUM(coi.subTotalPrice) " +
            "FROM CakeOrderItem coi " +
            "JOIN coi.cakeItem ci " +
            "JOIN coi.cakeOrder co " +
            "WHERE co.shop.shopId = :shopId " + // JPQL: co.shop.shopId
            "AND co.regDate BETWEEN :startDate AND :endDate " +
            "AND co.status = 'PICKUP_COMPLETED' " +
            "GROUP BY ci.cakeId, ci.cname " +
            "ORDER BY SUM(coi.quantity) DESC")
    List<Object[]> findProductSalesTableByShopIdAndRegDateBetweenAndStatusCompleted(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 4. 인기 랭킹 (상위 N개) 조회
    @Query(value = "SELECT ci.cake_id, ci.cname " +
            "FROM cake_order_item coi " +
            "JOIN cake_item ci ON coi.cake_id = ci.cake_id " +
            "JOIN cake_order co ON coi.order_id = co.order_id " +
            "WHERE co.shop_id = :shopId " +
            "AND co.reg_date BETWEEN :startDate AND :endDate " +
            "AND co.status = 'PICKUP_COMPLETED' " +
            "GROUP BY ci.cake_id, ci.cname ORDER BY SUM(coi.quantity) DESC LIMIT 3", nativeQuery = true)
    List<Object[]> findTop3RankingProductsNative(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    // 5. 아쉬운 랭킹 (하위 N개) 조회
    @Query(value = "SELECT ci.cake_id, ci.cname " +
            "FROM cake_order_item coi " +
            "JOIN cake_item ci ON coi.cake_id = ci.cake_id " + // cakeId
            "JOIN cake_order co ON coi.order_id = co.order_id " + // orderId
            "WHERE co.shop_id = :shopId " + // shopId
            "AND co.reg_date BETWEEN :startDate AND :endDate " +
            "AND co.status = 'PICKUP_COMPLETED' " +
            "GROUP BY ci.cake_id, ci.cname ORDER BY SUM(coi.quantity) ASC LIMIT 3", nativeQuery = true)
    List<Object[]> findLowest3RankingProductsNative(
            @Param("shopId") Long shopId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}