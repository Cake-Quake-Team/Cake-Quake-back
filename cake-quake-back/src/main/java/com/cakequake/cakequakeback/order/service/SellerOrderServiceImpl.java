package com.cakequake.cakequakeback.order.service;

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
        // 1) 주문 ID와 가게 ID를 조합해 CakeOrder를 조회 (없으면 예외)
        CakeOrder order = sellerOrderRepository.findByOrderIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다: " + orderId));

        // 2) BuyerInfo DTO 생성: 주문자 이름과 전화번호
        SellerOrderDetail.BuyerInfo buyer = SellerOrderDetail.BuyerInfo.builder()
                .uname(order.getMember().getUname())
                .phoneNumber(order.getMember().getPhoneNumber())
                .build();

        // 3) 주문에 속한 모든 CakeOrderItem을 조회하고 ProductDetail DTO로 변환
        List<SellerOrderDetail.ProductDetail> productDtos = cakeOrderItemRepository.findByCakeOrder_OrderId(orderId).stream()
                .map(item -> {
                    Map<Long, Integer> options = cakeOrderItemOptionRepository
                            .findByCakeOrderItem_OrderItemId(item.getOrderItemId())
                            .stream()
                            .collect(Collectors.toMap(
                                    oio -> oio.getCakeOptionMapping().getMappingId(),
                                    CakeOrderItemOption::getOptionCnt,
                                    Integer::sum
                            ));

                    return SellerOrderDetail.ProductDetail.builder()
                            .name(item.getCakeItem().getCname())
                            .quantity(item.getQuantity())
                            .unitPrice(item.getUnitPrice())
                            .subTotalPrice(item.getSubTotalPrice())
                            .thumbnailImageUrl(item.getCakeItem().getThumbnailImageUrl())
                            //.options(options)
                            .build();
                })
                .collect(Collectors.toList());

        // 4) 최종적으로 주문 상세 정보(Response DTO)에 필수 정보들을 담아서 반환
        return SellerOrderDetail.Response.builder()
                .orderId(order.getOrderId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus().name())
                .products(productDtos)
                .pickupDate(order.getPickupDate())
                .pickupTime(order.getPickupTime())
                .OrderTotalPrice(order.getOrderTotalPrice())
                .build();
    }

    //특정 가게(shopId)에서 해당 주문(orderId)의 상태를 업데이트
    @Override
    @Transactional
    public void updateOrderStatus(Long shopId, Long orderId, String status) {
        // 1) 주문과 가게 ID 조합으로 주문 조회 (없으면 예외)
        CakeOrder order = sellerOrderRepository.findByOrderIdAndShopId(orderId, shopId)
                .orElseThrow(() -> new NoSuchElementException("주문을 찾을 수 없습니다: " + orderId));

        // 2) 전달받은 문자열을 OrderStatus enum으로 변환
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(status);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("유효하지 않은 상태 값: " + status);
        }

        // 3) 현재 상태에서 새로운 상태로 전환이 가능한지 검증
        //    - RESERVATION_PENDING  → RESERVATION_CONFIRMED, RESERVATION_CANCELLED
        //    - RESERVATION_CONFIRMED → PICKUP_COMPLETED, NO_SHOW
        //    - (추가적으로 NO_SHOW이나 CANCEL 상태에서는 더 이상 변경 불가)
        OrderStatus current = order.getStatus();
        boolean validTransition = false;

        switch (current) {
            case RESERVATION_PENDING:
                if (newStatus == OrderStatus.RESERVATION_CONFIRMED ||
                        newStatus == OrderStatus.RESERVATION_CANCELLED) {
                    validTransition = true;
                }
                break;

            case RESERVATION_CONFIRMED:
                if (newStatus == OrderStatus.PICKUP_COMPLETED ||
                        newStatus == OrderStatus.NO_SHOW) {
                    validTransition = true;
                }
                break;

            case RESERVATION_CANCELLED:
                // 이미 취소된 주문 → 추가 처리(예: 로그, 사용자 알림 등) 후 예외
                throw new IllegalStateException("이미 예약이 취소된 상태입니다: " + current);

            case NO_SHOW:
                // 이미 노쇼 처리된 주문 → 예외
                throw new IllegalStateException("이미 노쇼로 처리된 상태입니다: " + current);

            case PICKUP_COMPLETED:
                // 이미 픽업 완료된 주문 → 예외
                throw new IllegalStateException("이미 픽업이 완료된 상태입니다: " + current);

            default:
                // (사실 위 다섯 가지 상태 외에는 없지만 혹시 생길 수 있는 상태를 대비)
                throw new IllegalStateException("허용되지 않는 상태 전환 요청입니다: " + current);
        }

    }
}