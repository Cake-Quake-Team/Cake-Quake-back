package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrderItemOption;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface CakeOrderItemOptionRepository extends JpaRepository<CakeOrderItemOption, Long> {
    @Query("SELECT oio FROM CakeOrderItemOption oio " +
            "JOIN FETCH oio.cakeOrderItem coi " + // CakeOrderItem도 필요하면 fetch join
            "JOIN FETCH oio.cakeOptionMapping com " + // CakeOptionMapping fetch join
            "JOIN FETCH com.optionItem oi " + // OptionItem fetch join
            "JOIN FETCH oi.optionType ot " + // ⭐ OptionType까지 fetch join ⭐
            "WHERE oio.cakeOrderItem.orderItemId IN :orderItemIds")
    List<CakeOrderItemOption> findByCakeOrderItem_OrderItemIdIn(@Param("orderItemIds") List<Long> orderItemIds);
}