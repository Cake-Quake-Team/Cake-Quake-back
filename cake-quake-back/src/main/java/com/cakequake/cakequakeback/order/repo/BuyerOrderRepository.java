package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface BuyerOrderRepository extends JpaRepository<CakeOrder, Long> {

    // CakeOrderItemRepository.java 에 추가
    // 여러 주문 ID에 속하는 CakeOrderItem 목록과 CakeItem 정보를 함께 가져옴
    @Query("SELECT coi FROM CakeOrderItem coi JOIN FETCH coi.cakeItem WHERE coi.cakeOrder.orderId IN :orderIds")
    List<CakeOrderItem> findByCakeOrder_OrderIdInWithCakeItem(@Param("orderIds") List<Long> orderIds);

    // 구매자 기준 페이징 주문 목록 조회
    Page<CakeOrder> findByMemberUserId(String userId, Pageable pageable);

    // 구매자 기준 특정 주문 상세 조회
    Optional<CakeOrder> findByOrderIdAndMemberUserId(
            @Param("orderId") Long orderId,
            @Param("userId") String userId
    );

    Optional<CakeOrder> findByOrderIdAndMemberUid(
            @Param("orderId") Long orderId,
            @Param("uid") Long uid
    );
    //주문 최신순으로 정렬
    Page<CakeOrder> findByMemberUserIdOrderByRegDateDesc(String userId, Pageable pageable);

    // --- 새로 추가되는 메서드 (주문 상세 DTO를 위한 Join Fetch) ---
    /**
     * 특정 주문 ID와 구매자 ID로 주문 상세 정보를 조회하며,
     * Member 및 Shop 엔티티를 즉시 로딩(FETCH JOIN)합니다.
     * @param orderId 조회할 주문의 ID
     * @param userId 구매자의 사용자 ID (String)
     * @return 해당 주문에 대한 CakeOrder 엔티티 (Member, Shop 정보 포함)
     */
    @Query("SELECT co FROM CakeOrder co JOIN FETCH co.member m JOIN FETCH co.shop s WHERE co.orderId = :orderId AND m.userId = :userId")
    Optional<CakeOrder> findByOrderIdAndMemberUserIdWithMemberAndShop(
            @Param("orderId") Long orderId,
            @Param("userId") String userId
    );

    /**
     * 특정 주문 ID와 구매자 UID로 주문 상세 정보를 조회하며,
     * Member 및 Shop 엔티티를 즉시 로딩(FETCH JOIN)합니다.
     * @param orderId 조회할 주문의 ID
     * @param uid 구매자의 UID (Long)
     * @return 해당 주문에 대한 CakeOrder 엔티티 (Member, Shop 정보 포함)
     */
    @Query("SELECT co FROM CakeOrder co JOIN FETCH co.member m JOIN FETCH co.shop s WHERE co.orderId = :orderId AND m.uid = :uid")
    Optional<CakeOrder> findByOrderIdAndMemberUidWithMemberAndShop(
            @Param("orderId") Long orderId,
            @Param("uid") Long uid
    );

    //------------뱃지-----------------------------------------------------

    // 전체 기간 회원의 주문 상태 건수 조회
    long countByMemberUidAndStatus(Long memberUid, OrderStatus status);

    // 특정 기간 내에 주문 수 조회
    long countByMemberUidAndStatusAndRegDateBetween(
            Long memberUid,
            OrderStatus status,
            LocalDateTime startDate,
            LocalDateTime endDate
    );

    // 주문 상태에 해당하는 주문 목록 조회
    List<CakeOrder> findByMemberUidAndStatus(Long memberUid, OrderStatus status);

    // 전체 기간 동안의 누적 금액 조회
    @Query("SELECT SUM(co.orderTotalPrice) FROM CakeOrder co WHERE co.member.uid = :memberUid AND co.status = :status")
    Long sumOrderTotalPriceByMemberUidAndStatus(@Param("memberUid") Long memberUid, @Param("status") OrderStatus status);

    // 회원의 특정 상태 목록 중 하나라도 특정 날짜 이후에 존재하는지 확인
    @Query("SELECT CASE WHEN COUNT(co) > 0 THEN TRUE ELSE FALSE END FROM CakeOrder co " +
            "WHERE co.member.uid = :memberUid " +
            "AND co.status IN :statuses " +
            "AND co.regDate >= :regDate")
    boolean existsByMemberUidAndStatusInAndRegDateAfter(
            @Param("memberUid") Long memberUid,
            @Param("statuses") List<OrderStatus> statuses,
            @Param("regDate") LocalDateTime regDate
    );
}