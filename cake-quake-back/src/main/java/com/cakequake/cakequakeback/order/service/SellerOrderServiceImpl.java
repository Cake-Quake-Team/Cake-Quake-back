package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderDetail;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderList;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.entities.CakeOrderItemOption;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemOptionRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.order.repo.SellerOrderRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {

    private final SellerOrderRepository sellerOrderRepository;
    private final CakeOrderItemRepository cakeOrderItemRepository;
    private final CakeOrderItemOptionRepository cakeOrderItemOptionRepository;

    //특정 가게(shopId)에 대한 주문 리스트를 페이징 처리하여 조회
    @Override
    public SellerOrderList.Response getShopOrderList(Long shopId, Pageable pageable) {
        // 1) 가게 ID로 CakeOrder 엔티티를 페이징 조회
        Page<CakeOrder> page = sellerOrderRepository.findByShopId(shopId, pageable);
        // 2) 조회된 CakeOrder 목록을 SellerOrderListItem DTO로 변환
        List<SellerOrderList.Response.SellerOrderListItem> dtoItems = page.getContent().stream()
                .map(order -> {
                    CakeOrderItem firstItem = cakeOrderItemRepository.findByCakeOrder_OrderId(order.getOrderId()).get(0);
                    String cname     = firstItem.getCakeItem().getCname();
                    String thumbnail = firstItem.getCakeItem().getThumbnailImageUrl();
                    Integer cnt      = firstItem.getQuantity();
                    Long total       = order.getOrderTotalPrice().longValue();

                    return SellerOrderList.Response.SellerOrderListItem.builder()
                            .orderId(order.getOrderId())
                            .orderNumber(order.getOrderNumber())
                            .cname(cname)
                            .thumbnailImageUrl(thumbnail)
                            .pickupDate(order.getPickupDate())
                            .pickupTime(order.getPickupTime())
                            .status(order.getStatus().name())
                            .productCnt(cnt)
                            .OrderTotalPrice(order.getOrderTotalPrice())
                            .build();
                })
                .collect(Collectors.toList());

        // 3) 페이지 정보(PageInfo) DTO 생성: 현재 페이지 번호, 전체 페이지 수, 전체 요소 개수
        SellerOrderList.Response.PageInfo pageInfo = SellerOrderList.Response.PageInfo.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalElements(page.getTotalElements())
                .build();

        // 4) 최종 응답 DTO에 목록과 페이지 정보를 담아서 반환
        return SellerOrderList.Response.builder()
                .orders(dtoItems)
                .pageInfo(pageInfo)
                .build();
    }
    //특정 가게(shopId)에 속한 단일 주문(orderId)의 상세 정보를 조회
    @Override
    public SellerOrderDetail.Response getShopOrderDetail(Long shopId, Long orderId) {
        CakeOrder order = sellerOrderRepository
                .findByOrderIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID));

        List<CakeOrderItem> items = cakeOrderItemRepository.findByCakeOrder_OrderId(orderId);
        List<SellerOrderDetail.ProductDetail> products = items.stream()
                .map(item -> {
                    Map<String, Integer> opts = cakeOrderItemOptionRepository
                            .findByCakeOrderItem_OrderItemId(item.getOrderItemId())
                            .stream()
                            .collect(Collectors.toMap(
                                    o -> o.getCakeOptionMapping().getMappingId().toString(),
                                    CakeOrderItemOption::getOptionCnt
                            ));
                    return SellerOrderDetail.ProductDetail.builder()
                            .name(item.getCakeItem().getCname())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subTotalPrice(item.getSubTotalPrice())
                            .thumbnailImageUrl(item.getCakeItem().getThumbnailImageUrl())
                            .options(opts)
                            .build();
                })
                .collect(Collectors.toList());

        SellerOrderDetail.BuyerInfo buyer = new SellerOrderDetail.BuyerInfo(
                order.getMember().getUname(),
                order.getMember().getPhoneNumber()
        );

        return SellerOrderDetail.Response.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .OrderTotalPrice(order.getOrderTotalPrice())
                .buyer(buyer)
                .products(products)
                .build();
    }


    //특정 가게(shopId)에서 해당 주문(orderId)의 상태를 업데이트
    @Override
    @Transactional
    public void updateOrderStatus(Long shopId, Long orderId, String statusStr) {
        // 1) 주문 조회
        CakeOrder order = sellerOrderRepository
                .findByOrderIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_ID));

        // 2) 문자열 → Enum 변환
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusStr);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException(ErrorCode.INVALID_TYPE);
        }

        // 3) 상태 전환 가능 여부 검증
        boolean valid;
        switch (order.getStatus()) {
            case RESERVATION_PENDING:
                valid = (newStatus == OrderStatus.RESERVATION_CONFIRMED ||
                        newStatus == OrderStatus.RESERVATION_CANCELLED);
                break;
            case RESERVATION_CONFIRMED:
                valid = (newStatus == OrderStatus.PICKUP_COMPLETED ||
                        newStatus == OrderStatus.NO_SHOW);
                break;
            case RESERVATION_CANCELLED:
                throw new BusinessException(ErrorCode.ORDER_MISMATCH,"이미 취소된 주문입니다.");
            case NO_SHOW:
                throw new BusinessException(ErrorCode.ORDER_MISMATCH,"이미 노쇼 처리된 주문입니다.");
            case PICKUP_COMPLETED:
                throw new BusinessException(ErrorCode.ORDER_MISMATCH,"이미 픽업 완료된 주문입니다.");
            default:
                throw new BusinessException(ErrorCode.ORDER_MISMATCH);
        }
        if (!valid) {
            throw new BusinessException(ErrorCode.ORDER_MISMATCH);
        }

        // 4) 리플렉션으로 private 필드 직접 변경
        try {
            var field = CakeOrder.class.getDeclaredField("status");
            field.setAccessible(true);
            field.set(order, newStatus);
        } catch (ReflectiveOperationException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR,"주문 상태 변경 실패");
        }

        // 5) Dirty checking 으로 트랜잭션 커밋 시점에 자동 반영됩니다.
    }

}