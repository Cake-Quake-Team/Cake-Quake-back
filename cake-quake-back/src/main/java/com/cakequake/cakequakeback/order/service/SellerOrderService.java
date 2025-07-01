package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.order.dto.seller.SellerOrderDetail;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderList;
import com.cakequake.cakequakeback.order.dto.seller.SellerStatistics;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import groovyjarjarantlr4.v4.runtime.misc.Nullable;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
@Transactional
public interface SellerOrderService {


    //판매자 주문 목록 조회
    SellerOrderList.Response getShopOrderList(Long shopId, Pageable pageable, @Nullable OrderStatus status);

    //판매자 주문 상세 조회
    SellerOrderDetail.Response getShopOrderDetail(Long shopId, Long orderId);

    //주문 상태 변경
    void updateOrderStatus(Long shopId, Long orderId, String status);

    //판매자 주문 통계 조회 메서드 추가
    SellerStatistics.Response getSellerStatistics(Long shopId, LocalDate startDate, LocalDate endDate);

    //판매자 주문 통계 관련 pdf 파일
    byte[] getSellerStatisticsPdf(Long shopId, LocalDate startDate, LocalDate endDate);
}
