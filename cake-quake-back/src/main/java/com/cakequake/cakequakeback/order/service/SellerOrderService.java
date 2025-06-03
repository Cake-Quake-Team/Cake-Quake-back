package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.order.dto.seller.SellerOrderDetail;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderList;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;


@Service
@Transactional
public interface SellerOrderService {


    //판매자 주문 목록 조회
    SellerOrderList.Response getShopOrderList(Long shopId, Pageable pageable);

    //판매자 주문 상세 조회
    SellerOrderDetail.Response getShopOrderDetail(Long shopId, Long orderId);

    //주문 상태 변경
    void updateOrderStatus(Long shopId, Long orderId, String status);
}
