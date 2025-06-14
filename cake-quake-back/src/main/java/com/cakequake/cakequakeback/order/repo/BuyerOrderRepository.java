package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.member.entities.Member;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional
public interface BuyerOrderRepository extends JpaRepository<CakeOrder, Long> {
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
}