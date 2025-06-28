package com.cakequake.cakequakeback.order.service;

import com.cakequake.cakequakeback.common.exception.BusinessException;
import com.cakequake.cakequakeback.common.exception.ErrorCode;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderDetail;
import com.cakequake.cakequakeback.order.dto.seller.SellerOrderList;
import com.cakequake.cakequakeback.order.dto.seller.SellerStatistics;
import com.cakequake.cakequakeback.order.entities.CakeOrder;
import com.cakequake.cakequakeback.order.entities.CakeOrderItem;
import com.cakequake.cakequakeback.order.entities.CakeOrderItemOption;
import com.cakequake.cakequakeback.order.entities.OrderStatus;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemOptionRepository;
import com.cakequake.cakequakeback.order.repo.CakeOrderItemRepository;
import com.cakequake.cakequakeback.order.repo.SellerOrderRepository;
import com.cakequake.cakequakeback.point.service.PointService;
import com.cakequake.cakequakeback.temperature.entities.Grade;
import com.cakequake.cakequakeback.temperature.entities.Temperature;
import com.cakequake.cakequakeback.temperature.repo.TemperatureRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.lang.Nullable; // ⭐ 이 임포트가 반드시 필요합니다. ⭐

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SellerOrderServiceImpl implements SellerOrderService {

    private final SellerOrderRepository sellerOrderRepository;
    private final CakeOrderItemRepository cakeOrderItemRepository;
    private final CakeOrderItemOptionRepository cakeOrderItemOptionRepository;
    private final PointService pointService;
    private final TemperatureRepository temperatureRepository;

    //특정 가게(shopId)에 대한 주문 리스트를 페이징 처리하여 조회
    @Override
    public SellerOrderList.Response getShopOrderList(Long shopId, Pageable pageable, @Nullable OrderStatus status) { // ⭐ @Nullable 어노테이션 추가 ⭐
        // 1) 가게 ID로 CakeOrder 엔티티를 페이징 조회
        Page<CakeOrder> page;
        // ⭐ status 파라미터에 따라 호출할 리포지토리 메서드를 분기 ⭐
        if (status != null) {
            page = sellerOrderRepository.findByShopIdAndStatus(shopId, status, pageable);
        } else {
            // status가 null이면 (즉, 모든 상태의 주문을 조회할 때) 기존 findByShopId 사용
            page = sellerOrderRepository.findByShopId(shopId, pageable);
        } // 2) 조회된 CakeOrder 목록을 SellerOrderListItem DTO로 변환
        List<SellerOrderList.Response.SellerOrderListItem> dtoItems = page.getContent().stream()
                .map(order -> {
                    List<CakeOrderItem> orderItems = cakeOrderItemRepository.findByCakeOrder_OrderId(order.getOrderId());
                    String cname = "상품 정보 없음";
                    String thumbnail = null;
                    Integer cnt = 0;

                    if (!orderItems.isEmpty()) {
                        CakeOrderItem firstItem = orderItems.get(0);
                        cname = firstItem.getCakeItem().getCname();
                        thumbnail = firstItem.getCakeItem().getThumbnailImageUrl();
                        cnt = firstItem.getQuantity();
                    }

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
                .orderNote(order.getOrderNote())
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

        System.out.println("DEBUG: Current Order Status (Before Change): " + order.getStatus()); // 디버그 로그
        System.out.println("DEBUG: Attempting to change to Status: " + statusStr); // 디버그 로그

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
            case RESERVATION_PENDING: // 예약 대기 중
                valid = (newStatus == OrderStatus.RESERVATION_CONFIRMED ||
                        newStatus == OrderStatus.RESERVATION_CANCELLED);
                break;
            case RESERVATION_CONFIRMED: // 예약 확정 (픽업 준비/픽업 완료/노쇼 가능)
                valid = (newStatus == OrderStatus.PREPARING || // 추가
                        newStatus == OrderStatus.READY_FOR_PICKUP || // 추가
                        newStatus == OrderStatus.PICKUP_COMPLETED ||
                        newStatus == OrderStatus.NO_SHOW);
                break;
            case PREPARING: // 준비 중
                valid = (newStatus == OrderStatus.READY_FOR_PICKUP ||
                        newStatus == OrderStatus.PICKUP_COMPLETED ||
                        newStatus == OrderStatus.RESERVATION_CANCELLED); // 준비 중에도 취소 가능 여부 (정책에 따라 추가)
                break;
            case READY_FOR_PICKUP: // 픽업 준비 완료
                valid = (newStatus == OrderStatus.PICKUP_COMPLETED ||
                        newStatus == OrderStatus.NO_SHOW); // 픽업 준비 완료 상태에서 취소는 보통 불가능
                break;
            case PICKUP_COMPLETED: // 픽업 완료 (더 이상 변경 불가, 최종 상태)
                valid = false; // 픽업 완료는 최종 상태이므로 더 이상 다른 상태로 변경할 수 없음
                break;
            case RESERVATION_CANCELLED: // 이미 취소됨 (변경 불가, 최종 상태)
                valid = false;
                break;
            case NO_SHOW: // 노쇼 처리됨 (변경 불가, 최종 상태)
                valid = false;
                break;
            default: // 정의되지 않거나 예상치 못한 현재 상태
                valid = false;
        }
        if (!valid) {
            System.out.println("DEBUG: Invalid Status Transition from " + order.getStatus() + " to " + newStatus); // 디버그 로그
            throw new BusinessException(ErrorCode.ORDER_MISMATCH,
                    String.format("현재 주문 상태 (%s) 에서 %s(으)로 변경할 수 없습니다.", order.getStatus(), newStatus));
        }

        // ⭐⭐⭐ 4) 상태 변경 로직 수정: 리플렉션 대신 엔티티의 setter 사용 ⭐⭐⭐
        // CakeOrder 엔티티에 public void setStatus(OrderStatus status) 또는 public void updateStatus(OrderStatus newStatus) 메서드가 있다고 가정
        order.updateStatus(newStatus); // 또는 order.setStatus(newStatus);
        // CakeOrder 엔티티가 @Getter @Setter (혹은 @Data) 롬복 어노테이션을 가지고 있다면 setStatus()는 자동 생성됩니다.
        // 아니면 CakeOrder 엔티티에 public void updateStatus(OrderStatus newStatus) { this.status = newStatus; } 메서드를 직접 추가해야 합니다.

        // 5) 명시적으로 저장 (선택 사항이지만 안전을 위해 추가)
        // @Transactional 어노테이션이 있으므로 Dirty Checking에 의해 자동 저장되지만,
        // 디버깅 목적으로는 명시적 저장이 도움이 될 수 있습니다.
        sellerOrderRepository.save(order);



        if (newStatus == OrderStatus.PICKUP_COMPLETED) {
            // (1) Temperature 엔티티에서 grade 조회
            var tempOpt = temperatureRepository.findByMember(order.getMember());
            Grade grade = tempOpt
                    .map(Temperature::getGrade)
                    .orElse(Grade.BASIC); // 없으면 BASIC으로 간주

            // (2) 등급별 적립율 결정
            double rate = getEarnRateByGrade(grade);

            // (3) 계산된 적립 포인트
            long earnedPoints = Math.round(order.getOrderTotalPrice() * rate);

            if (earnedPoints > 0) {
                pointService.changePoint(
                        order.getMember().getUid(),
                        earnedPoints,
                        String.format("구매 적립(%s 등급 %s%%)",
                                grade.name(),
                                rate * 100)
                );
            }
        }
        System.out.println("DEBUG: Order Status Successfully Updated to: " + order.getStatus()); // 디버그 로그
    }

    @Override
    public SellerStatistics.Response getSellerStatistics(Long shopId, LocalDate startDate, LocalDate endDate) {
        return null;
    }

    private double getEarnRateByGrade(Grade grade) {
        switch (grade) {
            case VIP:  return 0.01;   // 1%
            case VVIP: return 0.015;  // 1.5%
            default:   return 0.0;    // BASIC/FROZEN 은 적립 없음
        }
    }

}

