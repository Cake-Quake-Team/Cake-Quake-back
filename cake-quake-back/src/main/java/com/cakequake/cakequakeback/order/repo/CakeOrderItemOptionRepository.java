package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrderItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CakeOrderItemOptionRepository extends JpaRepository<CakeOrderItemOption, Long> {
    //특정 주문 아이템(orderItemId)에 속한 옵션 매핑 목록 조회
    List<CakeOrderItemOption> findByCakeOrderItem_OrderItemId(Long orderItemId);
}
