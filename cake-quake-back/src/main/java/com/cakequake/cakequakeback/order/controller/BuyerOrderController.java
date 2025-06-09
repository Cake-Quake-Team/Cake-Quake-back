package com.cakequake.cakequakeback.order.controller;

import com.cakequake.cakequakeback.order.dto.buyer.CreateOrder;
import com.cakequake.cakequakeback.order.dto.buyer.OrderDetail;
import com.cakequake.cakequakeback.order.dto.buyer.OrderList;
import com.cakequake.cakequakeback.order.service.BuyerOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/buyers/orders")
@RequiredArgsConstructor
public class BuyerOrderController {
    private final BuyerOrderService buyerOrderService;

    @PostMapping
    public ResponseEntity<CreateOrder.Response> createOrder(
            @AuthenticationPrincipal String userId,
            @Valid @RequestBody CreateOrder.Request createRequest
    ) {
        CreateOrder.Response response = buyerOrderService.createOrder(userId, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<OrderList.Response> getOrderList(
            @AuthenticationPrincipal String userId,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        OrderList.Response response = buyerOrderService.getOrderList(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("{orderId}") // 경로 명확화
    public ResponseEntity<OrderDetail.Response> getOrderDetail(
            @AuthenticationPrincipal String userId,
            @PathVariable Long orderId
    ) {
        OrderDetail.Response response = buyerOrderService.getOrderDetail(userId, orderId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal String userId,
            @PathVariable Long orderId
    ) {
        buyerOrderService.cancelOrder(userId, orderId);
        return ResponseEntity.noContent().build();
    }
}

