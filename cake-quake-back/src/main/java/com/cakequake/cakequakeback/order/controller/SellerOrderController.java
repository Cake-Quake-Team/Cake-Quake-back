package com.cakequake.cakequakeback.order.controller;

import com.cakequake.cakequakeback.order.dto.seller.SellerOrderDetail;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderList;
import com.cakequake.cakequakeback.order.dto.seller.SellerStatistics;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.service.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/shops/{shopId}")
@RequiredArgsConstructor
public class SellerOrderController {
    private final SellerOrderService sellerOrderService;

    /**
     * 판매자 주문 목록 조회
     */
    @GetMapping("/orders")
    public ResponseEntity<SellerOrderList.Response> getShopOrderList(
            @PathVariable Long shopId,
            Pageable pageable, @RequestParam(required = false) OrderStatus status) { // ⭐⭐ status @RequestParam 추가 ⭐⭐
        // ⭐⭐ 서비스 호출 시 status 파라미터 전달 ⭐⭐
        SellerOrderList.Response response = sellerOrderService.getShopOrderList(shopId, pageable, status);
        return ResponseEntity.ok(response);
    }

    /**
     * 판매자 주문 상세 조회
     */
    @GetMapping("orders/{orderId}")
    public ResponseEntity<SellerOrderDetail.Response> getShopOrderDetail(
            @PathVariable Long shopId,
            @PathVariable Long orderId) {
        SellerOrderDetail.Response response = sellerOrderService.getShopOrderDetail(shopId, orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상태 변경
     * 요청 본문에 {"status": "NEW_STATUS_VALUE"} 형식으로 데이터를 받습니다.
     */
    @PatchMapping("orders/{orderId}")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long shopId,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> payload) { // 간단한 상태 값 변경을 위해 Map 사용
        String status = payload.get("status");
        if (status == null || status.trim().isEmpty()) {
            // 적절한 예외 처리 또는 BadRequest 응답
            return ResponseEntity.badRequest().build();
        }
        sellerOrderService.updateOrderStatus(shopId, orderId, status);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics") // 기존 경로에 /statistics 추가
    public ResponseEntity<SellerStatistics.Response> getSellerOrderStatistics(
            @PathVariable Long shopId, // URL 경로에서 shopId를 가져옴
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // 날짜 파라미터가 없으면 기본값 설정 (예: 최근 30일)
        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        // startDate가 endDate보다 늦을 경우의 유효성 검사
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        SellerStatistics.Response statistics = sellerOrderService.getSellerStatistics(shopId, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
}
