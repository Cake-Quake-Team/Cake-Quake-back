package com.cakequake.cakequakeback.order.controller;

import com.cakequake.cakequakeback.order.dto.buyer.CreateOrder;
import com.cakequake.cakequakeback.order.dto.buyer.OrderDetail;
import com.cakequake.cakequakeback.order.dto.buyer.OrderList;
import com.cakequake.cakequakeback.order.service.BuyerOrderService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/buyer/orders")
public class BuyerOrderController {
    private final BuyerOrderService buyerOrderService;

    public BuyerOrderController(BuyerOrderService buyerOrderService) {
        this.buyerOrderService = buyerOrderService;
    }

    @PostMapping("/create")
    public ResponseEntity<CreateOrder.Response> createOrder(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @Valid @RequestBody CreateOrder.Request createRequest
    ) {
        CreateOrder.Response response = buyerOrderService.createOrder(userId, createRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<OrderList.Response> getOrderList(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @PageableDefault(size = 20, sort = "modDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        OrderList.Response response = buyerOrderService.getOrderList(userId, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}") // 경로 명확화
    public ResponseEntity<OrderDetail.Response> getOrderDetail(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @PathVariable Long orderId
    ) {
        OrderDetail.Response response = buyerOrderService.getOrderDetail(userId, orderId);
        return ResponseEntity.ok(response);

    }

    @PatchMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(
            @AuthenticationPrincipal(expression = "member.userId") String userId,
            @PathVariable Long orderId
    ) {
        buyerOrderService.cancelOrder(userId, orderId);
        return ResponseEntity.noContent().build();
    }
}

