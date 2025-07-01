package com.cakequake.cakequakeback.order.controller;

import com.cakequake.cakequakeback.order.dto.seller.SellerOrderDetail;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderList;
import com.cakequake.cakequakeback.order.dto.seller.SellerStatistics;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.service.SellerOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// HttpHeaders 임포트 수정
import org.springframework.http.HttpHeaders;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/shops/{shopId}") // 기존 매핑 경로 유지
@RequiredArgsConstructor
public class SellerOrderController {
    private final SellerOrderService sellerOrderService;

    /**
     * 판매자 주문 목록 조회
     */
    @GetMapping("/orders")
    public ResponseEntity<SellerOrderList.Response> getShopOrderList(
            @PathVariable Long shopId,
            Pageable pageable, @RequestParam(required = false) OrderStatus status) {
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
            @RequestBody Map<String, String> payload) {
        String status = payload.get("status");
        if (status == null || status.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        sellerOrderService.updateOrderStatus(shopId, orderId, status);
        return ResponseEntity.ok().build();
    }

    /**
     * 판매자 주문 통계 조회 (웹 화면용)
     */
    @GetMapping("/sales") // 기존 경로 유지
    public ResponseEntity<SellerStatistics.Response> getSellerOrderStatistics(
            @PathVariable Long shopId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().minusMonths(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate.isAfter(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        SellerStatistics.Response statistics = sellerOrderService.getSellerStatistics(shopId, startDate, endDate);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 판매자 주문 통계 관련 pdf파일
     */
    @GetMapping("/sales/pdf")
    public ResponseEntity<ByteArrayResource> downloadSellerStatisticsPdf(
            @PathVariable Long shopId,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate, // @DateTimeFormat 추가
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) { // @DateTimeFormat 추가

        // 서비스에서 PDF 파일의 byte 배열을 가져옴. (SellerOrderService에 메서드 시그니처 추가 필요)
        // getSellerStatisticsPdf 메서드를 찾을 수 없다는 오류는 SellerOrderService 인터페이스에 이 메서드가 없기 때문
        byte[] pdfBytes = sellerOrderService.getSellerStatisticsPdf(shopId, startDate, endDate);

        // 파일 이름 생성 (예: shopId_통계_20240101_20240630.pdf)
        String filename = String.format("seller_statistics_%d_%s_%s.pdf",
                shopId,
                startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")),
                endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        // 응답 헤더 설정
        HttpHeaders headers = new HttpHeaders(); // ⭐ Spring의 HttpHeaders 사용
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"");
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentLength(pdfBytes.length);

        // ByteArrayResource로 응답 본문 생성
        ByteArrayResource resource = new ByteArrayResource(pdfBytes);

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(pdfBytes.length)
                .contentType(MediaType.APPLICATION_PDF)
                .body(resource);
    }
}