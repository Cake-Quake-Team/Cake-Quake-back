package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrderItemOption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CakeOrderItemOptionRepository extends JpaRepository<CakeOrderItemOption, Long> {
    //특정 주문 아이템(orderItemId)에 속한 옵션 매핑 목록 조회
    // cakeOptionMapping과 더불어 그 안에 있는 optionItem까지 FETCH JOIN하도록 수정
    @EntityGraph(attributePaths = {"cakeOptionMapping", "cakeOptionMapping.optionItem"})
    List<CakeOrderItemOption> findByCakeOrderItem_OrderItemId(Long orderItemId);

    // 여러 주문 아이템(orderItemIds)에 속한 옵션 매핑 목록 조회
    // cakeOptionMapping, cakeOrderItem, 그리고 cakeOptionMapping 내의 optionItem까지 FETCH JOIN하도록 수정
    @EntityGraph(attributePaths = {"cakeOptionMapping", "cakeOptionMapping.optionItem", "cakeOrderItem"})
    List<CakeOrderItemOption> findByCakeOrderItem_OrderItemIdIn(List<Long> orderItemIds);

}
