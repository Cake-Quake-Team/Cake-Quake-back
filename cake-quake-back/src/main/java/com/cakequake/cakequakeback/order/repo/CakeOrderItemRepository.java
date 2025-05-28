package com.cakequake.cakequakeback.order.repo;

import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CakeOrderItemRepository extends JpaRepository<CakeOrderItem, Long> {

//    //특정 주문의 주문 아이템 조회
//    List<CakeOrderItem> findByCakeOrder(CakeOrder order);
}