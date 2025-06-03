package com.cakequake.cakequakeback.order.controller;

import com.cakequake.cakequakeback.order.dto.seller.SellerOrderDetail;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderList;
import com.cakequake.cakequakeback.order.service.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/seller/shops/{shopId}/orders")
@RequiredArgsConstructor
public class SellerOrderController {
    private final SellerOrderService sellerOrderService;

    /**
     * 판매자 주문 목록 조회
     */
    @GetMapping
    public ResponseEntity<SellerOrderList.Response> getShopOrderList(
            @PathVariable Long shopId,
            Pageable pageable) {
        // TODO: 해당 shopId에 대한 권한이 있는지 확인하는 로직 필요
        SellerOrderList.Response response = sellerOrderService.getShopOrderList(shopId, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * 판매자 주문 상세 조회
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<SellerOrderDetail.Response> getShopOrderDetail(
            @PathVariable Long shopId,
            @PathVariable Long orderId) {
        // TODO: 해당 shopId에 대한 권한이 있는지 확인하는 로직 필요
        SellerOrderDetail.Response response = sellerOrderService.getShopOrderDetail(shopId, orderId);
        return ResponseEntity.ok(response);
    }

    /**
     * 주문 상태 변경
     * 요청 본문에 {"status": "NEW_STATUS_VALUE"} 형식으로 데이터를 받습니다.
     */
    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(
            @PathVariable Long shopId,
            @PathVariable Long orderId,
            @RequestBody Map<String, String> payload) { // 간단한 상태 값 변경을 위해 Map 사용
        // TODO: 해당 shopId에 대한 권한이 있는지 확인하는 로직 필요
        String status = payload.get("status");
        if (status == null || status.trim().isEmpty()) {
            // 적절한 예외 처리 또는 BadRequest 응답
            return ResponseEntity.badRequest().build();
        }
        sellerOrderService.updateOrderStatus(shopId, orderId, status);
        return ResponseEntity.ok().build();
    }
}
